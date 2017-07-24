package com.NUNO.demo.controller;

import com.NUNO.demo.model.PackageVo;
import com.NUNO.demo.service.PackageService;
import io.swagger.api.NotFoundException;
import io.swagger.api.PackagesApi;
import io.swagger.model.PackageRequest;
import io.swagger.model.PackageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class PackagesControler implements PackagesApi {

    private final PackageService packageService;

    @Override
    public Callable<ResponseEntity<List<PackageResponse>>> getAllPackages() throws NotFoundException {
        return () -> {
            List<PackageResponse> get = packageService.getAllPackages().stream()
                    .map(PackageVo::voToResponse)
                    .collect(Collectors.<PackageResponse>toList());
            return ResponseEntity.status(HttpStatus.OK).body(get);
        };
    }

    @Override
    public Callable<ResponseEntity<PackageResponse>> createPackage(@RequestBody PackageRequest _package) throws NotFoundException {
        return () -> {

            PackageResponse create = packageService.createPackage(PackageVo.requestToVo(_package)).voToResponse();

            return ResponseEntity.status(HttpStatus.CREATED).body(create);
        };
    }

    @Override
    public Callable<ResponseEntity<PackageResponse>> getPackage(@PathVariable("packageId") Long packageId, @RequestParam(value = "currency", required = false) String currency) throws NotFoundException {
        return () -> {
            PackageResponse get = packageService.getPackage(packageId, currency).voToResponse();
            return ResponseEntity.status(HttpStatus.OK).body(get);
        };
    }

    @Override
    public Callable<ResponseEntity<PackageResponse>> updatePackage(@PathVariable Long packageId, @RequestBody PackageRequest packageRequest) throws NotFoundException {
        return () -> {

            PackageResponse create = packageService.updatePackage(packageId, PackageVo.requestToVo(packageRequest))
                    .voToResponse();

            return ResponseEntity.status(HttpStatus.OK).body(create);
        };
    }

    @Override
    public Callable<ResponseEntity<Void>> deletePackage(@PathVariable Long packageId) throws NotFoundException {
        return () -> {
            packageService.deletePackage(packageId);
            return ResponseEntity.ok().build();
        };
    }
}

