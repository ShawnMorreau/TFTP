package test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import app.Client;
import app.ClientController;

/**
 * This class will test the Client class.
 * 
 * @author SYSC3303 Project Team 12
 */
public class ClientTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testClientCanBeInstantiated() {
		// Creates a new instance of the client. If this fails, the test fails
		new Client(false, false);
	}

	@Test
	public void testClientControllerCanBeInstantiated() {
		// Creates a new instance of the client controller. If this fails, the test fails
		new ClientController();
	}
	
}
