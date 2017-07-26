package com.NUNO.demo.controller;

import com.NUNO.demo.api.generated.controller.PackagesApi;
import com.NUNO.demo.api.generated.dto.PackageRequest;
import com.NUNO.demo.api.generated.dto.PackageResponse;
import com.NUNO.demo.model.PackageVo;
import com.NUNO.demo.service.PackageService;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class PackagesControler implements PackagesApi {

    private final PackageService packageService;


    @Override
    public ResponseEntity<PackageResponse> createPackage(@RequestBody PackageRequest _package) {
        PackageResponse create = packageService.createPackage(PackageVo.requestToVo(_package)).voToResponse();

        return ResponseEntity.status(HttpStatus.CREATED).body(create);

    }

    @Override
    public ResponseEntity<Void> deletePackage(@PathVariable Long packageId) {
        packageService.deletePackage(packageId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<List<PackageResponse>> getAllPackages() {
        List<PackageResponse> get = packageService.getAllPackages().stream()
                .map(PackageVo::voToResponse)
                .collect(Collectors.<PackageResponse>toList());
        return ResponseEntity.status(HttpStatus.OK).body(get);

    }

    @Override
    public ResponseEntity<PackageResponse> getPackage(@ApiParam(value = "The ID of the package", required = true) @PathVariable("packageId") Long packageId,
                                                      @ApiParam(value = "The currency to be returned") @RequestParam(value = "currency", required = false) String currency) {
        PackageResponse get = packageService.getPackage(packageId, currency).voToResponse();
        return ResponseEntity.status(HttpStatus.OK).body(get);

    }

    @Override
    public ResponseEntity<PackageResponse> updatePackage(@PathVariable Long packageId, @RequestBody PackageRequest packageRequest) {
        PackageResponse create = packageService.updatePackage(packageId, PackageVo.requestToVo(packageRequest))
                .voToResponse();

        return ResponseEntity.status(HttpStatus.OK).body(create);

    }
}

