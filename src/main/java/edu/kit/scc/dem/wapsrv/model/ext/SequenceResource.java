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
package edu.kit.scc.dem.wapsrv.model.ext;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 *
 * @author jejkal
 */
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class SequenceResource implements Serializable{

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String sequenceIri;
  private String annotationIri;

  public SequenceResource(){
  }

  public static SequenceResource create(String sequenceIri, String annotationIri){
    SequenceResource res = new SequenceResource();
    res.setSequenceIri(sequenceIri);
    res.setAnnotationIri(annotationIri);
    return res;
  }

  public Long getId(){
    return id;
  }

  public void setId(Long id){
    this.id = id;
  }

  public String getSequenceIri(){
    return sequenceIri;
  }

  public void setSequenceIri(String sequenceIri){
    this.sequenceIri = sequenceIri;
  }

  public String getAnnotationIri(){
    return annotationIri;
  }

  public void setAnnotationIri(String annotationIri){
    this.annotationIri = annotationIri;
  }
}
