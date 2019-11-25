package org.mozi.varann.data;

import de.charite.compbio.jannovar.data.JannovarData;
import org.apache.ignite.springdata20.repository.IgniteRepository;
import org.apache.ignite.springdata20.repository.config.RepositoryConfig;
import org.springframework.stereotype.Repository;

@Repository
@RepositoryConfig(cacheName = "transcriptCache")
public interface TranscriptDbRepository extends IgniteRepository<JannovarData, String> {

}
