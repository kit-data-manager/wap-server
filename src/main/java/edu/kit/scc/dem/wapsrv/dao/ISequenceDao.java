/*
 * Copyright 2018 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.scc.dem.wapsrv.dao;

import edu.kit.scc.dem.wapsrv.model.ext.SequenceResource;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * DAO interface used by Spring Data in order to provide database access to
 * sequence information. In addition to the default interface methods provided
 * by the default JpaRepository, some findBy methods are provided in order to
 * access sequence resources more directly.
 *
 * @author jejkal
 */
public interface ISequenceDao extends JpaRepository<SequenceResource, Long>{

  /**
   * Find all sequence resources belonging to one sequence identified by
   * sequenceIri.
   *
   * @param sequenceIri The IRI of the sequence.
   *
   * @return A list of sequence resources.
   */
  List<SequenceResource> findBySequenceIriEquals(String sequenceIri);

  /**
   * Find a subset of sequence resources belonging to one sequence identified by
   * sequenceIri. Size and start index of the subset is defined by the provided
   * Pageable object.
   *
   * @param sequenceIri The IRI of the sequence.
   * @param pgbl The pageable object defining page and elements per page.
   *
   * @return A page of sequence resources containing max. 'elements per page'
   * elements.
   */
  Page<SequenceResource> findBySequenceIriEquals(String sequenceIri, Pageable pgbl);

  /**
   * Find a single sequence information associated with the provided
   * annotationIri.
   *
   * @param annotationIri The IRI of the annotation.
   *
   * @return An optional sequence resource.
   */
  Optional<SequenceResource> findByAnnotationIriEquals(String annotationIri);

}
