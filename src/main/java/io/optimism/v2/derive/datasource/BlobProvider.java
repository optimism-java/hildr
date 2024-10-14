package io.optimism.v2.derive.datasource;

import io.optimism.types.BlobSidecar;
import io.optimism.types.BlockInfo;
import java.util.List;

/**
 * the blob data provider.
 *
 * @author thinkAfCod
 * @since 0.4.5
 */
public interface BlobProvider {

    BlobSidecar getBlobSidercars(BlockInfo l1Info, List<String> blobHashes);
}
