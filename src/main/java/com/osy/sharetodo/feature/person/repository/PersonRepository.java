package com.osy.sharetodo.feature.person.repository;

import com.osy.sharetodo.feature.person.domain.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long> {
    Optional<Person> findByAccount_Id(Long id);
}
