package com.shop.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.shop.domain.CustomerOrder;

import com.shop.repository.CustomerOrderRepository;
import com.shop.web.rest.util.HeaderUtil;
import com.shop.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing CustomerOrder.
 */
@RestController
@RequestMapping("/api")
public class CustomerOrderResource {

    private final Logger log = LoggerFactory.getLogger(CustomerOrderResource.class);
        
    @Inject
    private CustomerOrderRepository customerOrderRepository;

    /**
     * POST  /customer-orders : Create a new customerOrder.
     *
     * @param customerOrder the customerOrder to create
     * @return the ResponseEntity with status 201 (Created) and with body the new customerOrder, or with status 400 (Bad Request) if the customerOrder has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/customer-orders",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<CustomerOrder> createCustomerOrder(@RequestBody CustomerOrder customerOrder) throws URISyntaxException {
        log.debug("REST request to save CustomerOrder : {}", customerOrder);
        if (customerOrder.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("customerOrder", "idexists", "A new customerOrder cannot already have an ID")).body(null);
        }
        CustomerOrder result = customerOrderRepository.save(customerOrder);
        return ResponseEntity.created(new URI("/api/customer-orders/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("customerOrder", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /customer-orders : Updates an existing customerOrder.
     *
     * @param customerOrder the customerOrder to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated customerOrder,
     * or with status 400 (Bad Request) if the customerOrder is not valid,
     * or with status 500 (Internal Server Error) if the customerOrder couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/customer-orders",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<CustomerOrder> updateCustomerOrder(@RequestBody CustomerOrder customerOrder) throws URISyntaxException {
        log.debug("REST request to update CustomerOrder : {}", customerOrder);
        if (customerOrder.getId() == null) {
            return createCustomerOrder(customerOrder);
        }
        CustomerOrder result = customerOrderRepository.save(customerOrder);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("customerOrder", customerOrder.getId().toString()))
            .body(result);
    }

    /**
     * GET  /customer-orders : get all the customerOrders.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of customerOrders in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/customer-orders",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<CustomerOrder>> getAllCustomerOrders(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of CustomerOrders");
        Page<CustomerOrder> page = customerOrderRepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/customer-orders");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /customer-orders/:id : get the "id" customerOrder.
     *
     * @param id the id of the customerOrder to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the customerOrder, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/customer-orders/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<CustomerOrder> getCustomerOrder(@PathVariable Long id) {
        log.debug("REST request to get CustomerOrder : {}", id);
        CustomerOrder customerOrder = customerOrderRepository.findOne(id);
        return Optional.ofNullable(customerOrder)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /customer-orders/:id : delete the "id" customerOrder.
     *
     * @param id the id of the customerOrder to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/customer-orders/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteCustomerOrder(@PathVariable Long id) {
        log.debug("REST request to delete CustomerOrder : {}", id);
        customerOrderRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("customerOrder", id.toString())).build();
    }

}
