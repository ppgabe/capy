package dev.ailuruslabs.capybackend.repository;

import dev.ailuruslabs.capybackend.domain.UserProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserProfileRepository extends MongoRepository<UserProfile, String> {
    Optional<UserProfile> findByEmail(String email);
    List<UserProfile> findByEmailEndingWith(String domain);
}
