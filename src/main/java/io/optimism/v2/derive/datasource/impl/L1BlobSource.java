package io.optimism.v2.derive.datasource.impl;

import io.optimism.types.BlobSidecar;
import io.optimism.types.BlockInfo;
import io.optimism.v2.derive.datasource.BlobProvider;
import java.util.List;

/**
 * the L1 blob data source
 *
 * @author thinkAfCod
 * @since 0.4.6
 */
public class L1BlobSource implements BlobProvider {
    @Override
    public BlobSidecar getBlobSidercars(BlockInfo l1Info, List<String> blobHashes) {
        return null;
    }
}
