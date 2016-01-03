package com.sp.spikes.cxf.endpoints;

import com.sp.spikes.cxf.domain.Customer;
import com.sp.spikes.cxf.repo.CustomerRepository;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class CustomerResourceEndpointIntegrationTest {

    private static final String ROOT_PATH = "http://localhost:9000";
    private static final String CUSTOMER_RESOURCE_PATH = String.format("%s/%s",ROOT_PATH,CustomerResourceEndpoint.ENDPOINT_PATH);

    private CustomerRepository customerRepository;
    private Server server;

    private JAXBContext jaxbContext;
    private Unmarshaller jaxbUnmarshaller ;
    private HttpClient httpClient;
    private Marshaller marshaller;

    @After
    public void tearDown(){
        server.destroy();
    }

    @Before
    public void setup() throws JAXBException {
        customerRepository = new CustomerRepository();
        CustomerResourceEndpoint customerResourceEndpoint = new CustomerResourceEndpoint(customerRepository);

        initializeCxf(customerResourceEndpoint);
        initializeJaxb();

        customerRepository.deleteAll();
        httpClient = new HttpClient();
    }


    @Test
    public void shouldReturnExistingCustomer() throws Exception {
        Customer expectedCustomer = new Customer(1L,"Test","Pune");
        customerRepository.upsert(expectedCustomer);

        GetMethod getMethod = new GetMethod(resourcePathFor(1));
        httpClient.executeMethod(getMethod);
        InputStream xmlStream = getMethod.getResponseBodyAsStream();
        Customer actualCustomer = (Customer)jaxbUnmarshaller.unmarshal(xmlStream);
        getMethod.releaseConnection();

        assertEquals(HttpStatus.SC_OK,getMethod.getStatusCode());
        assertEquals(expectedCustomer, actualCustomer);
    }

    @Test
    public void shouldCreateOrUpdateNewCustomer() throws Exception {
        Customer expectedCustomer = new Customer(1L,"Test","Pune");
        customerRepository.upsert(expectedCustomer);

        PostMethod postMethod = postMethodFor(expectedCustomer);

        httpClient.executeMethod(postMethod);
        postMethod.releaseConnection();

        assertEquals(HttpStatus.SC_NO_CONTENT,postMethod.getStatusCode());
        assertEquals(1,customerRepository.getAll().size());
        assertEquals(expectedCustomer,customerRepository.get(1L));
    }

    @Test
    public void shouldDeleteCustomer() throws Exception {
        Customer expectedCustomer1 = new Customer(1L,"Test1","Pune1");
        Customer expectedCustomer2 = new Customer(2L,"Test2","Pune2");
        customerRepository.upsert(expectedCustomer1);
        customerRepository.upsert(expectedCustomer2);

        DeleteMethod deleteMethod = new DeleteMethod(resourcePathFor(1));

        httpClient.executeMethod(deleteMethod);
        deleteMethod.releaseConnection();

        assertEquals(HttpStatus.SC_NO_CONTENT, deleteMethod.getStatusCode());
        assertEquals(1,customerRepository.getAll().size());
        assertEquals(expectedCustomer2,customerRepository.get(2L));
    }

    private PostMethod postMethodFor(Customer expectedCustomer) throws JAXBException {
        PostMethod postMethod = new PostMethod(CUSTOMER_RESOURCE_PATH);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        marshaller.marshal(expectedCustomer,byteArrayOutputStream);
        ByteArrayRequestEntity byteArrayRequestEntity = new ByteArrayRequestEntity(byteArrayOutputStream.toByteArray(),"text/xml");
        postMethod.setRequestEntity(byteArrayRequestEntity);
        return postMethod;
    }

    private String resourcePathFor(long customerId) {
        return String.format("%s/%s",CUSTOMER_RESOURCE_PATH, customerId);
    }

    private void initializeCxf(CustomerResourceEndpoint customerResourceEndpoint) {
        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
        sf.setResourceClasses(CustomerResourceEndpoint.class);
        sf.setResourceProvider(CustomerResourceEndpoint.class,
                new SingletonResourceProvider(customerResourceEndpoint));
        sf.setAddress(ROOT_PATH);
        server = sf.create();
    }

    private void initializeJaxb() throws JAXBException {
        this.jaxbContext = JAXBContext.newInstance(Customer.class, ArrayList.class);
        this.jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        this.marshaller = jaxbContext.createMarshaller();
    }


}