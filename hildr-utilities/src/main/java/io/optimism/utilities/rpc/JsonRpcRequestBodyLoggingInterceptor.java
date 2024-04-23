package io.optimism.utilities.rpc;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueue;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonRpcRequestBodyLoggingInterceptor extends AbstractExecutionThreadService implements Interceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonRpcRequestBodyLoggingInterceptor.class);

    private MpscArrayQueue<String> queue;

    private int fileId = 0;

    private String outputParentPath = "./json_raw/";

    int dataLength = 0;
    List<String> requestCache = new ArrayList<>();

    private final String outputFileNamePostfix = "_request.debug.json";

    private final String outputFileNameFormat = "%d" + outputFileNamePostfix;

    private final Function<String, Boolean> shouldLog;

    private AtomicBoolean shutdown = new AtomicBoolean(false);

    public JsonRpcRequestBodyLoggingInterceptor(Function<String, Boolean> shouldLog) {
        queue = new MpscArrayQueue<>(10000);
        File outputParent = Path.of(outputParentPath).toFile();
        if (outputParent.isFile()) {
            throw new IllegalArgumentException("Output parent path is a file");
        }
        if (!outputParent.exists()) {
            try {
                Files.createDirectories(outputParent.toPath());
            } catch (IOException e) {
                throw new IllegalStateException("Failed to create output parent directory");
            }
        }
        AtomicInteger fileIdAtomic = new AtomicInteger(0);
        File[] unused = outputParent.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.endsWith(outputFileNamePostfix)) {
                    String idStr = name.replace(outputFileNamePostfix, "");
                    int id = Integer.parseInt(idStr);
                    if (id > fileIdAtomic.get()) {
                        fileIdAtomic.set(id);
                    }
                }
                return false;
            }
        });
        this.fileId = fileIdAtomic.get();
        this.shouldLog = shouldLog;
        this.startAsync();
    }

    @NotNull @Override
    public Response intercept(@NotNull final Chain chain) throws IOException {
        Request request = chain.request();
        Buffer buffer = new Buffer();
        if (request.body() == null) {
            return chain.proceed(request);
        }
        request.body().writeTo(buffer);
        String requestBody = buffer.readUtf8();
        if (!shouldLog.apply(requestBody)) {
            return chain.proceed(request);
        }
        queue.offer(requestBody);
        return chain.proceed(request);
    }

    @Override
    protected void run() throws Exception {
        for (; ; ) {
            if (shutdown.get() && dataLength == 0) {
                break;
            }
            readRequest();
            Thread.sleep(250);
        }
        LOGGER.info("logging service break");
    }

    @Override
    protected void triggerShutdown() {
        super.triggerShutdown();
        shutdown.compareAndExchange(false, true);
        LOGGER.info("logging service shutdown, but will write file before");
        try {
            writeReq();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void readRequest() throws IOException {
        for (var body = this.queue.poll(); body != null; body = this.queue.poll()) {
            dataLength += body.getBytes(UTF_8).length;
            requestCache.add(body);
            LOGGER.info("add body to queue: cache size: {}, dateLength: {}", requestCache.size(), dataLength);
            if (dataLength > 200 * 1024) {
                writeReq();
            }
        }
    }

    private void writeReq() throws IOException {
        LOGGER.info("will write cache to file: dateLength: {}", dataLength);
        writeToFile(requestCache);
        requestCache = new ArrayList<>();
        dataLength = 0;
    }

    private void writeToFile(List<String> bodyies) throws IOException {
        var outputFilePath = Path.of(outputParentPath, String.format(outputFileNameFormat, fileId));
        var outputFile = outputFilePath.toFile();
        if (outputFile.length() > 1024 * 1024 * 500) {
            compressFile(outputFile, true);
            fileId += 1;
            outputFilePath = Path.of(outputParentPath, String.format(outputFileNameFormat, fileId));
        }
        try (BufferedWriter writer = Files.newBufferedWriter(outputFilePath, UTF_8, CREATE, APPEND)) {
            for (int i = 0; i < bodyies.size(); i++) {
                writer.write(bodyies.get(i));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void compressFile(final File file, final boolean deleteOriginalFile) {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            scope.fork(() -> {
                Path zipFilePath = Path.of(file.getParent(), "zipped", file.getName() + ".zip");
                LOGGER.info("will create json raw zip file: {}", zipFilePath);
                if (!zipFilePath.getParent().toFile().exists()) {
                    Files.createDirectories(zipFilePath.getParent());
                }
                Files.deleteIfExists(zipFilePath);
                FileOutputStream fos = new FileOutputStream(zipFilePath.toString());
                ZipOutputStream zos = new ZipOutputStream(fos);
                zos.setLevel(9);
                File fileToZip = new File(file.getPath());

                FileInputStream fis = new FileInputStream(fileToZip);
                ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                zos.putNextEntry(zipEntry);

                byte[] bytes = new byte[4096];
                int length;
                while ((length = fis.read(bytes)) >= 0) {
                    zos.write(bytes, 0, length);
                }
                zos.closeEntry();
                fis.close();
                zos.close();
                fos.close();
                if (deleteOriginalFile) {
                    file.delete();
                }
                return null;
            });
            scope.join();
            scope.throwIfFailed();
        } catch (ExecutionException | InterruptedException e) {
            throw new IllegalStateException("Failed to compress file", e);
        }
    }
}
