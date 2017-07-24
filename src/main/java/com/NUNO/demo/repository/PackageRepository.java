package com.NUNO.demo.repository;

import com.NUNO.demo.entity.Package;
import org.springframework.data.repository.CrudRepository;

public interface PackageRepository extends CrudRepository<Package, Long> {
}
