package org.mozi.varann.data;

import htsjdk.variant.vcf.VCFFileReader;
import org.apache.ignite.springdata20.repository.IgniteRepository;
import org.apache.ignite.springdata20.repository.config.RepositoryConfig;
import org.springframework.stereotype.Repository;

@Repository
@RepositoryConfig(cacheName = "genomeCache")
public interface GenomeDbRepository extends IgniteRepository<VCFFileReader, String> {
}
