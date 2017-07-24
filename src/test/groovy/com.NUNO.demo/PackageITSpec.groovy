package com.NUNO.demo

import com.NUNO.demo.entity.Package
import com.NUNO.demo.entity.Product
import com.NUNO.demo.repository.PackageRepository
import com.NUNO.demo.repository.ProductRepository
import io.swagger.model.ErrorResponse
import io.swagger.model.PackageRequest
import io.swagger.model.PackageResponse
import io.swagger.model.ProductRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import spock.lang.Unroll

@ContextConfiguration
@SpringBootTest(classes = [DemoApplication], webEnvironment = WebEnvironment.RANDOM_PORT)
class PackageITSpec extends Specification {

    @Autowired
    TestRestTemplate restTemplate

    @Autowired
    ProductRepository productRepo

    @Autowired
    PackageRepository packageRepo

    @Value('${local.server.port}')
    int port

    def "GET All endpoint"() {
        given: "A Product"
        def product = productRepo.save(new Product(name: "product",
                externalId: UUID.randomUUID(),
                usdPrice: new BigDecimal(100)
        ))

        and: "A Package that contains the previous product"
        def aPackage = packageRepo.save(new Package(name: "package",
                description: "package description",
                productList: [product]))

        when: "GET All Packages"
        PackageResponse[] response = restTemplate.getForObject("http://localhost:" + port + "/packages", PackageResponse[].class)

        then: "The added packages found"
        response.size() > 0
        response.any {
            it.id == aPackage.id && it.name == aPackage.name &&
                    it.description == aPackage.description && it.products.size() == aPackage.productList.size() &&
                    it.products.first().id == product.externalId && it.products.first().price == product.usdPrice
        }

        cleanup:
        packageRepo.deleteAll()
        productRepo.deleteAll()
    }

    def "GET All Empty"() {
        when: "GET All Packages"
        PackageResponse[] response = restTemplate.getForObject("http://localhost:" + port + "/packages", PackageResponse[].class)

        then: "The result matches"
        response.size() == 0
        cleanup:
        packageRepo.deleteAll()
        productRepo.deleteAll()
    }

    def "GET endpoint no price conversion"() {
        given: "A Product"
        def product = productRepo.save(new Product(name: "product",
                externalId: UUID.randomUUID(),
                usdPrice: new BigDecimal(100)
        ))

        and: "A Package that contains the previous product"
        def aPackage = packageRepo.save(new Package(name: "package",
                description: "package description",
                productList: [product]))

        when: "GET package"
        def result = restTemplate.getForObject("http://localhost:" + port + "/packages/" + aPackage.id, PackageResponse.class)

        then: "The result matches the database"
        result.price == product.usdPrice
        result.name == aPackage.name
        result.products.size() == aPackage.productList.size()
        [result.products.sort { it.id }, aPackage.productList.sort { it.id }].transpose().collect {
            assert it[0].id == it[1].externalId;
            assert it[0].name == it[1].name;
            assert it[0].price == it[1].usdPrice;
        }

        cleanup:
        packageRepo.deleteAll()
        productRepo.deleteAll()
    }

    @Unroll
    def "GET endpoint price as #currency"() {
        given: "A Product"
        def product = productRepo.save(new Product(name: "product",
                externalId: UUID.randomUUID(),
                usdPrice: new BigDecimal(100)
        ))

        and: "A Package"
        def aPackage = packageRepo.save(new Package(name: "package",
                description: "package description",
                productList: [product]))

        when: "GET package"
        def result = restTemplate.getForObject("http://localhost:" + port + "/packages/" + aPackage.id + "?currency=" + currency, PackageResponse.class)

        then: "The result matches and the currency was converted"
        if (currency == "USD") {
            assert result.price == product.usdPrice
        } else {
            assert result.price != product.usdPrice
        }
        result.name == aPackage.name
        result.products.size() == aPackage.productList.size()
        [result.products.sort { it.id }, aPackage.productList.sort { it.id }].transpose().collect {
            if (currency == "USD") {
                assert it[0].price == it[1].usdPrice
            } else {
                assert it[0].price != it[1].usdPrice
            }
            assert it[0].id == it[1].externalId
            assert it[0].name == it[1].name
        }

        cleanup:
        packageRepo.deleteAll()
        productRepo.deleteAll()

        where:
        currency << ["EUR", "GBP", "USD"]
    }

    def "Get endpoint no such package"() {
        when: "Get endpoint is called"
        def result = restTemplate.getForObject("http://localhost:" + port + "/packages/" + (Math.random() * 9999), ErrorResponse.class)

        then: "An error is returned"

        result.errors.size() == 1
        result.errors.any { it.fieldName == "ID" && it.message.contains("Package with ID not found:") }
    }

    def "Delete a package"() {
        given: "A Product"
        def product = productRepo.save(new Product(name: "product",
                externalId: UUID.randomUUID(),
                usdPrice: new BigDecimal(100)
        ))

        and: "A Package that contains the previous product"
        def aPackage = packageRepo.save(new Package(name: "package",
                description: "package description",
                productList: [product]))

        when: "Delete package"
        restTemplate.delete("http://localhost:" + port + "/packages/" + aPackage.id)

        then: "The Packages no longer exists"
        packageRepo.findAll().every { it.id != aPackage.id }

        and: "The product still exists"
        productRepo.findAll().any { it.id == product.id }

        cleanup:
        packageRepo.deleteAll()
        productRepo.deleteAll()
    }

    def "POST a package"() {
        given: "A Product"
        def product = productRepo.save(new Product(name: "product",
                externalId: UUID.randomUUID(),
                usdPrice: new BigDecimal(100)
        ))

        and: "A Package request"
        def packageRequest = new PackageRequest(name: "New Package",
                description: "description",
                products: [new ProductRequest(id: product.externalId)])

        when: "POST package"
        def response = restTemplate
                .postForObject("http://localhost:" + port + "/packages", packageRequest, PackageResponse.class)


        then: "The result matches"
        response.price == product.usdPrice
        response.name == packageRequest.name
        response.products.size() == packageRequest.products.size()
        [response.products.sort { it.id }, packageRequest.products.sort { it.id }].transpose().collect {
            assert it[0].id == it[1].id
        }

        cleanup:
        packageRepo.deleteAll()
        productRepo.deleteAll()
    }

    def "POST a package with invalid product"() {
        given: "A Package request"
        def packageRequest = new PackageRequest(name: "New Package",
                description: "description",
                products: [new ProductRequest(id: UUID.randomUUID())])

        when: "POST package"
        def response = restTemplate
                .postForObject("http://localhost:" + port + "/packages", packageRequest, ErrorResponse.class)


        then: "An error is returned"
        response.errors.size() == 1
        response.errors.any { it.fieldName == "ID" && it.message.contains("Product with ID not found:") }

        cleanup:
        packageRepo.deleteAll()
    }

    def "Update a package"() {
        given: "A Product"
        def product = productRepo.save(new Product(name: "product",
                externalId: UUID.randomUUID(),
                usdPrice: new BigDecimal(100)
        ))

        and: "A Package that contains the previous product"
        def aPackage = packageRepo.save(new Package(name: "package",
                description: "package description",
                productList: [product]))

        and: "A Package request"
        def packageRequest = new PackageRequest(name: "New Package",
                description: "description",
                products: [new ProductRequest(id: product.externalId),
                           new ProductRequest(id: product.externalId)])

        when: "POST package"
        restTemplate.put("http://localhost:" + port + "/packages/" + aPackage.id, packageRequest)

        then: "The result matches the request"
        def dbPackage = packageRepo.findOne aPackage.id
        dbPackage.name == packageRequest.name
        dbPackage.productList.size() == packageRequest.products.size()
        [dbPackage.productList.sort { it.externalId }, packageRequest.products.sort { it.id }].transpose().collect {
            assert it[0].externalId == it[1].id
        }

        cleanup:
        packageRepo.deleteAll()
        productRepo.deleteAll()
    }


}