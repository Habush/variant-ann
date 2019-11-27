package org.mozi.varann.data;

import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import org.apache.ignite.springdata20.repository.IgniteRepository;
import org.apache.ignite.springdata20.repository.config.RepositoryConfig;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RepositoryConfig(cacheName = "genomeCache")
public interface GenomeDbRepository extends IgniteRepository<List<VariantContext>, String> {
}
