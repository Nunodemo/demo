package com.NUNO.demo.service;

import com.NUNO.demo.entity.Package;
import com.NUNO.demo.entity.Product;
import com.NUNO.demo.exception.PackageNotFoundException;
import com.NUNO.demo.model.PackageVo;
import com.NUNO.demo.repository.PackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class PackageService {

    private final PackageRepository packageRepository;
    private final ProductService productService;
    private final ForeignExchangeService foreignExchangeService;

    public PackageVo createPackage(PackageVo packageVo) {
        Package aPackage = packageVo.voToEntity(null);

        resolveProductList(aPackage);

        return PackageVo.entityToVo(packageRepository.save(aPackage));
    }

    private void resolveProductList(Package aPackage) {
        aPackage.setProductList(
                aPackage.getProductList().stream()
                        .map(product -> productService.getExistingProduct(product.getExternalId()))
                        .collect(Collectors.<Product>toList())
        );
    }

    public PackageVo updatePackage(Long packageId, PackageVo packageVo) {
        Package aPackage = packageVo.voToEntity(getExistingPackage(packageId));

        resolveProductList(aPackage);

        return PackageVo.entityToVo(packageRepository.save(aPackage));
    }

    public List<PackageVo> getAllPackages() {
        return StreamSupport.stream(packageRepository.findAll().spliterator(), false)
                .map(PackageVo::entityToVo)
                .collect(Collectors.<PackageVo>toList());
    }

    public PackageVo getPackage(Long id, String currency) {
        PackageVo result = PackageVo.entityToVo(getExistingPackage(id));
        if (currency != null && !currency.equals("USD")) {
            result = foreignExchangeService.convertCurrency(result, currency);
        }

        return result;
    }

    public void deletePackage(Long id) {
        Package p = getExistingPackage(id);
        packageRepository.delete(p);
    }

    private Package getExistingPackage(Long id) {
        return Optional.ofNullable(packageRepository.findOne(id))
                .orElseThrow(() -> new PackageNotFoundException(id.toString()));
    }
}
