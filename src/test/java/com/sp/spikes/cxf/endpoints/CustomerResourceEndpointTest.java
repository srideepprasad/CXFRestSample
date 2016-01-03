package com.sp.spikes.cxf.endpoints;

import com.sp.spikes.cxf.domain.Customer;
import com.sp.spikes.cxf.repo.CustomerRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CustomerResourceEndpointTest {

    private CustomerResourceEndpoint customerResourceEndpoint;
    @Mock
    private CustomerRepository customerRepository;

    @Before
    public void setup(){
        customerResourceEndpoint = new CustomerResourceEndpoint(customerRepository);
    }

    @Test
    public void shouldDeleteCustomer(){
        customerRepository.delete(1L);

        verify(customerRepository).delete(1L);
    }

    @Test
    public void shouldGetCustomer(){
        Customer expectedCustomer = new Customer(1L, "Name", "Address");
        when(customerRepository.get(1L)).thenReturn(expectedCustomer);

        Customer actualCustomer = customerResourceEndpoint.get(1L);
        assertSame(expectedCustomer,actualCustomer);
    }

    @Test
    public void shouldUpsertCustomer(){
        Customer expectedCustomer = new Customer(1L, "Name", "Address");

        customerResourceEndpoint.updateOrCreate(expectedCustomer);

        verify(customerRepository).upsert(expectedCustomer);
    }

}