package com.example.cache.service;

import com.example.cache.model.Product;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

@Service
public class ProductService {

	private static final String ProductCacheKey = "ProductCache";

	@Autowired
	private RedisTemplate<String, Product> redisTemplate;

	private HashOperations<String, Long, Product> productCache;

	@PostConstruct
	public void init() {
		this.productCache = redisTemplate.opsForHash();
	}

	@HystrixCommand(fallbackMethod = "readProductFromCache", commandKey = "GetProduct", threadPoolKey = "GetProductFromEndpoint")
	public Product getProduct(Long productId) {
		System.out.println("Sending request to endpoint");
		RestTemplate restTemplate;
		ResponseEntity<Product> result;
		restTemplate = new RestTemplate();
		result = restTemplate.exchange("http://localhost:8878/product/edit/" + productId, HttpMethod.GET, null,
				new ParameterizedTypeReference<Product>() {
				});
		if (result.getStatusCode() == HttpStatus.OK) {
			System.out.println("Request successful");
			System.out.println(result.getBody());
			Product product = result.getBody();
			this.updateCache(product);
			return product;
		} else {
			System.out.println("Cannot access user endpoint");
			throw new RuntimeException("Exception: Cannot access user endpoint");
		}
	}

	@HystrixCommand(fallbackMethod = "defaultFallback", commandKey = "GetProduct", threadPoolKey = "GetProductFromCache")
	private Product readProductFromCache(Long productId) {
		System.out.println("Reading product from cache");
		return productCache.get(ProductCacheKey, productId);

	}

	private Product defaultFallback(Long productId) {
		System.out.println("Cannot read user from cache with the id: " + productId);
		System.out.println("Returning null user");
		return null;
	}

	private void updateCache(Product product) {
		try {
			System.out.println("Updating cache...");
			productCache.put(ProductCacheKey, product.getId(), product);
			System.out.println("cache updated");
		} catch (Throwable e) {
			System.out.println("Cannot update cache" + e);
		}
	}
}