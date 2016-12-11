package com.shop.domain;


import javax.persistence.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A CustomerOrder.
 */
@Entity
@Table(name = "customer_order")
public class CustomerOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "create_time")
    private ZonedDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public CustomerOrder customerId(String customerId) {
        this.customerId = customerId;
        return this;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public ZonedDateTime getCreateTime() {
        return createTime;
    }

    public CustomerOrder createTime(ZonedDateTime createTime) {
        this.createTime = createTime;
        return this;
    }

    public void setCreateTime(ZonedDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CustomerOrder customerOrder = (CustomerOrder) o;
        if(customerOrder.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, customerOrder.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "CustomerOrder{" +
            "id=" + id +
            ", customerId='" + customerId + "'" +
            ", createTime='" + createTime + "'" +
            '}';
    }
}
