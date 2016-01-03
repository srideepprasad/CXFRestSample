package com.sp.spikes.cxf.repo;

import com.sp.spikes.cxf.domain.Customer;

import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;

/**
 * Created by deepu on 3/1/16.
 */
public class CustomerRepository {
    private final Hashtable<Long,Customer> customers = new Hashtable<Long, Customer>(); //Needs to be synchronized - hence hastable

    public Customer get(Long id){
        return customers.get(id);
    }

    public void delete(Long id){
        checkIfCustomerExists(id);
        customers.remove(id);
    }

    public void upsert(Customer customer) {
        customers.put(customer.getId(),customer);
    }

    public Collection<Customer> getAll(){
        return Collections.unmodifiableCollection(customers.values());
    }

    public void deleteAll(){
        customers.clear();
    }

    private void checkIfCustomerExists(Long id) {
        if (!customers.containsKey(id)){
            throw new IllegalArgumentException(String.format("Customer ID %s does not exist", id));
        }
    }
}
