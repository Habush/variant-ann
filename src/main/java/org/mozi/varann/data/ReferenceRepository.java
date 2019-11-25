package org.mozi.varann.data;

import de.charite.compbio.jannovar.vardbs.base.AlleleMatcher;
import org.apache.ignite.springdata20.repository.IgniteRepository;
import org.apache.ignite.springdata20.repository.config.RepositoryConfig;
import org.springframework.stereotype.Repository;

@Repository
@RepositoryConfig(cacheName = "refCache")
public interface ReferenceRepository extends IgniteRepository<AlleleMatcher, String> {
}
