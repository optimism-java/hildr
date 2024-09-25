package io.optimism.spec.derive.datasource;

import io.optimism.types.BlobSidecar;
import io.optimism.types.BlockInfo;

import java.util.List;

public interface BlobProvider {

  BlobSidecar getBlobs(BlockInfo l1Info, List<String> blobHashes);

}
