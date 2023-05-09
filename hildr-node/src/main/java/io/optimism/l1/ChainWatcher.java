package io.optimism.l1;

import io.optimism.config.Config;
import java.math.BigInteger;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.jctools.queues.MpscBlockingConsumerArrayQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.tuples.generated.Tuple2;

/**
 * the ChainWatcher class.
 *
 * @author thinkAfCod
 * @since 2023.05
 */
public class ChainWatcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(ChainWatcher.class);

  private Future<Object> handle;

  private Config config;

  private BigInteger l1StartBlock;

  private BigInteger l2StartBlock;

  private Queue<BlockUpdate> blockUpdateReceiver;

  private ExecutorService executor;

  public ChainWatcher(BigInteger l1StartBlock, BigInteger l2StartBlock, Config config) {
    this.config = config;
    this.l1StartBlock = l1StartBlock;
    this.l2StartBlock = l2StartBlock;
    this.blockUpdateReceiver = new MpscBlockingConsumerArrayQueue(1000);
    this.executor = new ThreadPoolExecutor(
        1, 4, 30,
        TimeUnit.MINUTES, new LinkedBlockingDeque<>(300));
  }

  public void start() {
    if (handle != null && !handle.isDone()) {
      handle.cancel(false);
    }

    Tuple2<Future, BlockingQueue<BlockUpdate>> tuple =
        startWatcher(this, this.l1StartBlock, this.l2StartBlock, this.config);
    this.handle = tuple.component1();
    this.blockUpdateReceiver = tuple.component2();
  }

  public void restart(BigInteger l1StartBlock, BigInteger l2StartBlock) {
    if (handle != null && !handle.isDone()) {
      handle.cancel(false);
    }
    Tuple2<Future, BlockingQueue<BlockUpdate>> tuple =
        startWatcher(this, l1StartBlock, l2StartBlock, this.config);
    this.handle = tuple.component1();
    this.blockUpdateReceiver = tuple.component2();
    this.l1StartBlock = l1StartBlock;
    this.l2StartBlock = l2StartBlock;
  }

  public void stop() {
    if (handle != null && !handle.isDone()) {
      handle.cancel(false);
    }
  }

  private static Tuple2<Future, BlockingQueue<BlockUpdate>> startWatcher(
      final ChainWatcher chainWatcher,
      BigInteger l1StartBlock, BigInteger l2StartBlock, Config config) {
    final BlockingQueue<BlockUpdate> queue = new MpscBlockingConsumerArrayQueue<>(1000);
    Future future = chainWatcher.executor.submit(() -> {
      final InnerWatcher watcher = new InnerWatcher(
          config, queue, l1StartBlock, l2StartBlock);
      for (; ; ) {
        LOGGER.debug("fetching L1 data for block {}", watcher.currentBlock);
        try {
          watcher.tryIngestBlock();
        } catch (Exception e) {
          LOGGER.warn(
              "failed to fetch data for block {}: {}",
              watcher.currentBlock,
              e);
        }
      }
    });
    return new Tuple2(future, queue);
  }

}
