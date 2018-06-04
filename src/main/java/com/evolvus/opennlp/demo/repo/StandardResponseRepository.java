package com.evolvus.opennlp.demo.repo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.evolvus.opennlp.demo.response.StandardResponse;

@Repository
public interface StandardResponseRepository extends MongoRepository<StandardResponse, String> {

	public List<StandardResponse> findByActionIntentName(final String intentName);
	
}
