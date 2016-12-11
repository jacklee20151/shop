package com.shop.web.rest;

import com.shop.ShopApp;

import com.shop.config.SecurityBeanOverrideConfiguration;

import com.shop.domain.CustomerOrder;
import com.shop.repository.CustomerOrderRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.hasItem;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the CustomerOrderResource REST controller.
 *
 * @see CustomerOrderResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ShopApp.class, SecurityBeanOverrideConfiguration.class})
public class CustomerOrderResourceIntTest {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("Z"));

    private static final String DEFAULT_CUSTOMER_ID = "AAAAA";
    private static final String UPDATED_CUSTOMER_ID = "BBBBB";

    private static final ZonedDateTime DEFAULT_CREATE_TIME = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneId.systemDefault());
    private static final ZonedDateTime UPDATED_CREATE_TIME = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
    private static final String DEFAULT_CREATE_TIME_STR = dateTimeFormatter.format(DEFAULT_CREATE_TIME);

    @Inject
    private CustomerOrderRepository customerOrderRepository;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Inject
    private EntityManager em;

    private MockMvc restCustomerOrderMockMvc;

    private CustomerOrder customerOrder;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        CustomerOrderResource customerOrderResource = new CustomerOrderResource();
        ReflectionTestUtils.setField(customerOrderResource, "customerOrderRepository", customerOrderRepository);
        this.restCustomerOrderMockMvc = MockMvcBuilders.standaloneSetup(customerOrderResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CustomerOrder createEntity(EntityManager em) {
        CustomerOrder customerOrder = new CustomerOrder()
                .customerId(DEFAULT_CUSTOMER_ID)
                .createTime(DEFAULT_CREATE_TIME);
        return customerOrder;
    }

    @Before
    public void initTest() {
        customerOrder = createEntity(em);
    }

    @Test
    @Transactional
    public void createCustomerOrder() throws Exception {
        int databaseSizeBeforeCreate = customerOrderRepository.findAll().size();

        // Create the CustomerOrder

        restCustomerOrderMockMvc.perform(post("/api/customer-orders")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(customerOrder)))
                .andExpect(status().isCreated());

        // Validate the CustomerOrder in the database
        List<CustomerOrder> customerOrders = customerOrderRepository.findAll();
        assertThat(customerOrders).hasSize(databaseSizeBeforeCreate + 1);
        CustomerOrder testCustomerOrder = customerOrders.get(customerOrders.size() - 1);
        assertThat(testCustomerOrder.getCustomerId()).isEqualTo(DEFAULT_CUSTOMER_ID);
        assertThat(testCustomerOrder.getCreateTime()).isEqualTo(DEFAULT_CREATE_TIME);
    }

    @Test
    @Transactional
    public void getAllCustomerOrders() throws Exception {
        // Initialize the database
        customerOrderRepository.saveAndFlush(customerOrder);

        // Get all the customerOrders
        restCustomerOrderMockMvc.perform(get("/api/customer-orders?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.[*].id").value(hasItem(customerOrder.getId().intValue())))
                .andExpect(jsonPath("$.[*].customerId").value(hasItem(DEFAULT_CUSTOMER_ID.toString())))
                .andExpect(jsonPath("$.[*].createTime").value(hasItem(DEFAULT_CREATE_TIME_STR)));
    }

    @Test
    @Transactional
    public void getCustomerOrder() throws Exception {
        // Initialize the database
        customerOrderRepository.saveAndFlush(customerOrder);

        // Get the customerOrder
        restCustomerOrderMockMvc.perform(get("/api/customer-orders/{id}", customerOrder.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(customerOrder.getId().intValue()))
            .andExpect(jsonPath("$.customerId").value(DEFAULT_CUSTOMER_ID.toString()))
            .andExpect(jsonPath("$.createTime").value(DEFAULT_CREATE_TIME_STR));
    }

    @Test
    @Transactional
    public void getNonExistingCustomerOrder() throws Exception {
        // Get the customerOrder
        restCustomerOrderMockMvc.perform(get("/api/customer-orders/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCustomerOrder() throws Exception {
        // Initialize the database
        customerOrderRepository.saveAndFlush(customerOrder);
        int databaseSizeBeforeUpdate = customerOrderRepository.findAll().size();

        // Update the customerOrder
        CustomerOrder updatedCustomerOrder = customerOrderRepository.findOne(customerOrder.getId());
        updatedCustomerOrder
                .customerId(UPDATED_CUSTOMER_ID)
                .createTime(UPDATED_CREATE_TIME);

        restCustomerOrderMockMvc.perform(put("/api/customer-orders")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(updatedCustomerOrder)))
                .andExpect(status().isOk());

        // Validate the CustomerOrder in the database
        List<CustomerOrder> customerOrders = customerOrderRepository.findAll();
        assertThat(customerOrders).hasSize(databaseSizeBeforeUpdate);
        CustomerOrder testCustomerOrder = customerOrders.get(customerOrders.size() - 1);
        assertThat(testCustomerOrder.getCustomerId()).isEqualTo(UPDATED_CUSTOMER_ID);
        assertThat(testCustomerOrder.getCreateTime()).isEqualTo(UPDATED_CREATE_TIME);
    }

    @Test
    @Transactional
    public void deleteCustomerOrder() throws Exception {
        // Initialize the database
        customerOrderRepository.saveAndFlush(customerOrder);
        int databaseSizeBeforeDelete = customerOrderRepository.findAll().size();

        // Get the customerOrder
        restCustomerOrderMockMvc.perform(delete("/api/customer-orders/{id}", customerOrder.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<CustomerOrder> customerOrders = customerOrderRepository.findAll();
        assertThat(customerOrders).hasSize(databaseSizeBeforeDelete - 1);
    }
}
