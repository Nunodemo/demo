package com.NUNO.demo

import com.NUNO.demo.entity.Package
import com.NUNO.demo.entity.Product
import com.NUNO.demo.generated.client.ApiClient
import com.NUNO.demo.generated.client.api.PackagesApi
import com.NUNO.demo.generated.client.model.ErrorResponse
import com.NUNO.demo.generated.client.model.PackageRequest
import com.NUNO.demo.generated.client.model.PackageResponse
import com.NUNO.demo.generated.client.model.ProductRequest
import com.NUNO.demo.repository.PackageRepository
import com.NUNO.demo.repository.ProductRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.ContextConfiguration
import retrofit2.Response
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.ThreadLocalRandom

@ContextConfiguration
@SpringBootTest(classes = [DemoApplication], webEnvironment = WebEnvironment.RANDOM_PORT)
class PackageITSpec extends Specification {

    PackagesApi packagesApi

    @Autowired
    ProductRepository productRepo

    @Autowired
    PackageRepository packageRepo

    @Autowired
    ObjectMapper objectMapper

    @Value('${local.server.port}')
    int port

    def setup() {
        packagesApi = generateClient(PackagesApi) as PackagesApi
    }

    def "GET All endpoint"() {
        given: "A Product"
        def product = createProduct()

        and: "A Package that contains the previous product"
        def aPackage = createPackage([product])

        when: "GET All Packages"
        def response = calling({ packagesApi.getAllPackages().execute() }, 200) as PackageResponse[]

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
        def response = calling({ packagesApi.getAllPackages().execute() }, 200) as PackageResponse[]

        then: "The result matches"
        response.size() == 0
    }

    def "GET endpoint no price conversion"() {
        given: "A Product"
        def product = createProduct()

        and: "A Package that contains the previous product"
        def aPackage = createPackage([product])

        when: "GET package"
        def response = calling({ packagesApi.getPackage(aPackage.id, null).execute() }, 200) as PackageResponse

        then: "The result matches the database"
        response.price == product.usdPrice
        response.name == aPackage.name
        response.products.size() == aPackage.productList.size()
        [response.products.sort { it.id }, aPackage.productList.sort { it.id }].transpose().collect {
            assert it[0].id == it[1].externalId
            assert it[0].name == it[1].name
            assert it[0].price == it[1].usdPrice
        }

        cleanup:
        packageRepo.deleteAll()
        productRepo.deleteAll()
    }


    @Unroll
    def "GET endpoint price as #currency"() {
        given: "A Product"
        def product = createProduct()

        and: "A Package"
        def aPackage = createPackage([product])

        when: "GET package"
        def response = calling({ packagesApi.getPackage(aPackage.id, currency).execute() }, 200) as PackageResponse

        then: "The result matches and the currency was converted"
        if (currency == "USD") {
            assert response.price == product.usdPrice
        } else {
            assert response.price != product.usdPrice
        }
        response.name == aPackage.name
        response.products.size() == aPackage.productList.size()
        [response.products.sort { it.id }, aPackage.productList.sort { it.id }].transpose().collect {
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
        def response = calling({
            packagesApi.getPackage(ThreadLocalRandom.current().nextLong(99999), null).execute()
        }, 404) as ErrorResponse

        then: "An error is returned"

        response.errors.size() == 1
        response.errors.any { it.fieldName == "ID" && it.message.contains("Package with ID not found:") }
    }

    def "Delete a package"() {
        given: "A Product"
        def product = createProduct()

        and: "A Package that contains the previous product"
        def aPackage = createPackage([product])

        when: "Delete package"
        calling({ packagesApi.deletePackage(aPackage.id).execute() }, 200)

        then: "The Packages no longer exists"
        packageRepo.findAll().every { it.id != aPackage.id }

        and: "The product still exists"
        productRepo.findAll().any { it.id == product.id }

        cleanup:
        packageRepo.deleteAll()
        productRepo.deleteAll()
    }

    def "Delete endpoint no such package"() {
        when: "Delete endpoint is called"
        def response = calling({
            packagesApi.deletePackage(ThreadLocalRandom.current().nextLong(99999)).execute()
        }, 404) as ErrorResponse

        then: "An error is returned"

        response.errors.size() == 1
        response.errors.any { it.fieldName == "ID" && it.message.contains("Package with ID not found:") }
    }


    def "POST a package"() {
        given: "A Product"
        def product = createProduct()

        and: "A Package request"
        def packageRequest = new PackageRequest(name: "New Package",
                description: "description",
                products: [new ProductRequest(id: product.externalId)])

        when: "POST package"
        def response = calling({ packagesApi.createPackage(packageRequest).execute() }, 201) as PackageResponse

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
        def response = calling({ packagesApi.createPackage(packageRequest).execute() }
                , 404) as ErrorResponse

        then: "An error is returned"
        response.errors.size() == 1
        response.errors.any { it.fieldName == "ID" && it.message.contains("Product with ID not found:") }

        cleanup:
        packageRepo.deleteAll()
    }

    def "Update a package"() {
        given: "A Product"
        def product = createProduct()

        and: "A Package that contains the previous product"
        def aPackage = createPackage([product])

        and: "A Package request"
        def packageRequest = new PackageRequest(name: "New Package",
                description: "description",
                products: [new ProductRequest(id: product.externalId),
                           new ProductRequest(id: product.externalId)])

        when: "POST package"
        calling({ packagesApi.updatePackage(aPackage.id, packageRequest).execute() }, 200)

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

    def "Update a package Invalid product"() {
        given: "A Package"
        def aPackage = createPackage([])

        and: "A Package request"
        def packageRequest = new PackageRequest(name: "New Package",
                description: "description",
                products: [new ProductRequest(id: ThreadLocalRandom.current().nextLong(99999))])

        when: "POST package"
        def response = calling({
            packagesApi.updatePackage(aPackage.id, packageRequest).execute()
        }, 404) as ErrorResponse

        then: "An error is returned"
        response.errors.size() == 1
        response.errors.any { it.fieldName == "ID" && it.message.contains("Product with ID not found:") }


        cleanup:
        packageRepo.deleteAll()
    }

    private Package createPackage( products) {
        packageRepo.save(new Package(name: "package",
                description: "package description",
                productList: products))
    }

    private Product createProduct() {
        productRepo.save(new Product(name: "product",
                externalId: UUID.randomUUID(),
                usdPrice: new BigDecimal(100)
        ))
    }

    def calling(Closure methodToCall, int expectedResponseCode) {
        def response = methodToCall.call() as Response
        assert response.code() == expectedResponseCode

        if (response.successful) {

            return response.body()
        } else {
            return objectMapper.readValue(response.errorBody().string(), ErrorResponse)
        }
    }

    def generateClient(Class c) {
        new ApiClient()
                .adapterBuilder
                .baseUrl("http://localhost:" + port + "/")
                .build()
                .create(c)
    }

}