/**
 * CookieListTest
 * Version 1.0
 * Author: Jian Cao, Ruolin Wang
 * Date : 01/17/2018
 * Class: <CSI 4321>
 * Copyright Reserved
 * 
 */
package G8R.serialization.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import G8R.serialization.CookieList;
import G8R.serialization.MessageInput;
import G8R.serialization.MessageOutput;
import G8R.serialization.ValidationException;

/**
 * CookieList test
 */
class CookieListTest {
	/**
	 * Method test
	 */
	@DisplayName("Method test")
	@Nested
	class Methodtest {
		@DisplayName("HashCode() Eqaul")
		@Test
		/**
		 * test HashCode() can detect whether two same value object has same hash code.
		 */
		void testHashCodeEqual() throws ValidationException {
			CookieList cookieFirst = new CookieList(); // equals and hashCode check name field value
			CookieList cookieSecond = new CookieList();
			cookieFirst.add("a", "1");
			cookieFirst.add("b", "1");
			cookieFirst.add("ab", "1");

			cookieSecond.add("a", "1");
			cookieSecond.add("b", "1");
			cookieSecond.add("ab", "1");
			assertNotSame(cookieFirst, cookieSecond); // don't cheat
			assertEquals(cookieFirst.hashCode(), cookieSecond.hashCode());
		}

		/**
		 * test HashCode() can detect whether two different value object has different
		 * hash code.
		 */
		@DisplayName("HashCode() NotEqual")
		@Test
		void testHashCodeNotEqual() throws ValidationException {
			CookieList cookieFirst = new CookieList(); // hashCode check name field value
			CookieList cookieSecond = new CookieList();
			cookieFirst.add("a", "1");
			cookieFirst.add("b", "1");
			cookieFirst.add("ab", "1");

			cookieSecond.add("a", "2");
			cookieSecond.add("b", "1");
			cookieSecond.add("ab", "1");
			assertNotSame(cookieFirst, cookieSecond); // don't cheat
			assertNotEquals(cookieFirst.hashCode(), cookieSecond.hashCode());
		}

		/**
		 * test when input of Encode() is null.
		 * 
		 * @exception java.lang.NullPointerException if MessageOutput is null
		 */
		@DisplayName("EncodeOutpuT Null")
		@Test
		void testEncodeOutpuTNullException() {
			MessageOutput temp = null;
			CookieList cook_temp = new CookieList();
			try {
				cook_temp.encode(temp);
				fail("No exception thrown.");
			} catch (Exception e) {
				assertTrue(e instanceof NullPointerException);
			}
		}

		/**
		 * test when input of Encode() is not null and the output is in order
		 * 
		 * @throws IOException
		 * @throws ValidationException
		 * @throws FileNotFoundException
		 * @throws UnsupportedEncodingException
		 * @exception java.lang.NullPointerException if MessageOutput is null
		 */
		@DisplayName("EncodeOutpuT Null")
		@Test
		void testEncodeOutpuTInOrder()
				throws ValidationException, FileNotFoundException, NullPointerException, UnsupportedEncodingException {

			OutputStream out = new ByteArrayOutputStream();
			MessageOutput messageOutput = new MessageOutput(out);
			CookieList cookielist = new CookieList();
			cookielist.add("a", "1");
			cookielist.add("b", "55555");
			try {
				cookielist.encode(messageOutput);
				String data = out.toString();
				assertEquals(data, "a=1\r\nb=55555\r\n\r\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		/**
		 * test when input of Encode() is not null and the output is in order
		 * 
		 * @throws IOException
		 * @throws ValidationException
		 * @exception java.lang.NullPointerException if MessageOutput is null
		 * @throws FileNotFoundException 
		 * @throws UnsupportedEncodingException 
		 */
		@DisplayName("EncodeOutpuT() IOException")
		@Test
		void testEncodeOutpuTIOException()
				throws ValidationException, NullPointerException, FileNotFoundException, UnsupportedEncodingException {

			OutputStream out = new FileOutputStream("test.out");
			MessageOutput messageOutput = new MessageOutput(out);
			CookieList cookielist = new CookieList();
			cookielist.add("a", "1");
			cookielist.add("b", "55555");
			try {
				out.close();
				cookielist.encode(messageOutput);
			} catch (IOException e) {
				assertTrue(e instanceof IOException);
			}

		}

		/**
		 * test add function with normal input
		 * @throws ValidationException 
		 *  
		 * 
		 */
		@DisplayName("add() successfully")
		@Test
		void testAdd() throws ValidationException {
			CookieList test = new CookieList();
			String name = "a";
			test.add(name, "1");
		}

		/**
		 * test add function with null name
		 * 
		 * the input is null  
		 * 
		 */
		@DisplayName("add() name null")
		@Test
		void testAddOnNameNull() throws ValidationException {
			CookieList test = new CookieList();
			String name = null;
			try {
				test.add(name, "1");
				fail("No exception thrown.");
			} catch (Exception e) {
				assertTrue(e instanceof NullPointerException);
				assertTrue(e.getMessage().contentEquals("name is null"));
			}
		}

		/**
		 * test add function with null value
		 * 
		 * the input is null  
		 * 
		 */
		@DisplayName("add() value null")
		@Test
		void testAddOnValueNull() throws ValidationException {
			CookieList test = new CookieList();
			String value = null;
			try {
				test.add("1", value);
				fail("No exception thrown.");
			} catch (Exception e) {
				assertTrue(e instanceof NullPointerException);
				assertTrue(e.getMessage().contentEquals("value is null"));
			}
		}

		/**
		 * test add function with invalid name
		 *  
		 * 
		 */
		@DisplayName("add() name invalid")
		@Test
		void testAddOnNameInValid() {
			CookieList testCookie = new CookieList();
			String name = "*a";
			try {
				testCookie.add(name, "1");
				fail("No exception thrown.");
			} catch (Exception e) {
				assertTrue(e instanceof ValidationException);
				assertTrue(e.getMessage().contains("name is not alphanumeric"));
			}
		}

		/**
		 * test add function with invalid value
		 *   
		 */
		@DisplayName("add() value invalid")
		@Test
		void testAddOnValueInValid() {
			CookieList testCookie = new CookieList();
			String value = "     sdd";
			try {
				testCookie.add("1", value);
				fail("No exception thrown.");
			} catch (Exception e) {
				assertTrue(e instanceof ValidationException);
				assertTrue(e.getMessage().contains("value is not alphanumeric"));
			}
		}

		/**
		 * test ToString() is under format "Cookies=[a=1,b=1]" when CookieList is not empty
		 *
		 */
		@DisplayName("ToString() NotEmpty")
		@Test
		void testToStringNotEmpty() throws ValidationException {
			CookieList cookie = new CookieList();
			cookie.add("a", "1a");
			cookie.add("b", "2b");
			assertEquals(cookie.toString(), "Cookies=[a=1a,b=2b]");
		}

		/**
		 * test ToString() is under format "Cookies=[]" when CookieList is empty
		 *
		 */
		@DisplayName("ToString() Empty")
		@Test
		void testToStringEmpty() throws ValidationException {
			CookieList cookie = new CookieList();
			assertEquals(cookie.toString(), "Cookies=[]");
		}

		/**
		 * test GetNames()
		 *
		 */
		@DisplayName("GetNames() True")
		@Test
		void testGetNames() throws ValidationException {
			CookieList cookie = new CookieList();
			cookie.add("a", "1");
			cookie.add("b", "1");
			Set<String> testSet = new TreeSet<>();
			testSet.add("a");
			testSet.add("b");
			assertEquals(cookie.getNames(), testSet);
		}

		/**
		 * test GetValue()
		 *
		 */
		@DisplayName("GetValue() True")
		@Test
		void testGetValue() throws ValidationException {
			CookieList cookie = new CookieList();
			cookie.add("a", "1");
			cookie.add("b", "2");
			assertEquals(cookie.getValue(""), null);
		}

		/**
		 * test whether EqualsObject() can return string representation of cookie list
		 * on the condition two objects exist and are the same class. Construct two
		 * equal CookieList to test whether they are equal.
		 */
		@DisplayName("EqualsObject() same")
		@Test
		void testEqualsObjectOnExistedEqual() throws ValidationException {
			CookieList cookie = new CookieList();

			cookie.add("a", "1");
			cookie.add("b", "1");
			cookie.add("a", "2");
			cookie.add("ab", "1");
			cookie.add("bb", "1");
			cookie.add("c", "1");
			cookie.add("b", "3");
			CookieList testCookie = new CookieList(cookie);
			assertEquals(cookie.equals(testCookie), true);
		}

		/**
		 * test whether EqualsObject() can return string representation of cookie list
		 * on the condition two objects exist and are the same class. Construct two
		 * different CookieList to test whether they are equal.
		 */
		@DisplayName("EqualsObject() different")
		@Test
		void testEqualsObjectOnExistedNotEqual() throws ValidationException {
			CookieList cookie = new CookieList();

			cookie.add("a", "1");
			cookie.add("b", "1");
			CookieList testCookie = new CookieList(cookie);
			cookie.add("a", "2");
			cookie.add("ab", "1");
			cookie.add("bb", "1");
			cookie.add("c", "1");
			cookie.add("b", "3");

			assertEquals(cookie.equals(testCookie), false);

		}

		/**
		 * test whether EqualsObject() can return string representation of cookie list
		 * on the condition two objects dont't exist. It tests that if object is null,
		 * it should return not equal.
		 */
		@DisplayName("EqualsObject() null")
		@Test
		void testEqualsObjectOnNull() throws ValidationException {
			CookieList testCookie = new CookieList();
			CookieList nullCookie = null;
			assertNotEquals(testCookie, nullCookie);
		}

		/**
		 * test whether EqualsObject() can return string representation of cookie list
		 * on the condition two objects are not the same class. It tests that if objects
		 * are not the same class, it should return not equal.
		 */
		@DisplayName("EqualsObject() different object")
		@Test
		void testEqualsObjectOnDifferObject() throws ValidationException {
			CookieList testCookie = new CookieList();
			String str = "1";

			assertEquals(testCookie.equals(str), false);
		}
	}

	@DisplayName("Constructor Test")
	@Nested
	class ConstructorTest {
		/**
		 * test CookieList constructor with a CookieList input parameter
		 * 
		 * the input is null  
		 * 
		 */
		@DisplayName("CookieList(CookieList)  CookieList = Null")
		@Test
		void testCookieListCloneNullClass() {
			// CookieList temp = new CookieList();
			CookieList temp = null;
			try {
				new CookieList(temp);
				fail("No exception thrown.");
			} catch (Exception e) {
				assertTrue(e instanceof NullPointerException);
				assertTrue(e.getMessage().contentEquals("cookieList is null"));
			}
		}

		/**
		 * test the situation that no validation happens when using the Constructor with input parameter
		 * MessageInput in.
		 * When the getBytes("UTF-8"), also should be right.
		 */
		@DisplayName("CookieList(MessageInput) succfessful")
		@Test
		void testCookieListConstructorNoValidation() throws NullPointerException, ValidationException, IOException {
			String testStr = "a=1\r\nb=2zxcc\r\n\r\nc=3\r\nd=4\r\n\r\n";
			InputStream testIn = new ByteArrayInputStream(testStr.getBytes("UTF-8"));
			MessageInput messageInput = new MessageInput(testIn);

			new CookieList(messageInput);
			new CookieList(messageInput);
			// System.out.println(cookieListTest.toString());
		}

		/**
		 * test the situation that validation happens when using the Constructor with input parameter
		 * MessageInput in.
		 * 
		 * The validation is that the input is null
		 * 
		 */
		@DisplayName("CookieList(MessageInput) MessageInput Null")
		@Test
		void testCookieListConstructorOnNullInput() throws ValidationException, IOException {
			try {
				MessageInput msg = null;
			    new CookieList(msg);
				fail("No exception thrown.");
			} catch (Exception e) {
				assertTrue(e instanceof NullPointerException);
			}
		}

		/**
		 * test the situation that validation happens when using the Constructor with input parameter
		 * MessageInput in.
		 * 
		 * 1.The validation is the name contains wrong elements
		 * 2.The validation is the name contains wrong elements
		 * 3.The validation is the cookie loses value
		 * such as : 
		 * a = ; 
		 * 4. The validation is the cookie loses value
		 * such as : 
		 * = 2; 
		 * 5.	 * The validation is the cookie is null 
		 * 6.The validation is the value contains wrong elements
		 * 7.\r\n\r\n
		 */
		@DisplayName("CookieList(MessageInput) wrong ")
		@ParameterizedTest()
		@ValueSource(strings = { "\"a=1*\\r\\nb=2\\r\\n\\r\\nc=3\\r\\nd=4\\r\\n\\r\\n\"",
				"a=1\r\nb*=2\r\n\r\nc=3\r\nd=4\r\n\r\n", "a=\r\nb=2\r\n\r\nc=3\r\nd=4\r\n\r\n",
				"a=1\r\n=2\r\n\r\nc=3\r\nd=4\r\n\r\n", "a=1*\\r\\nb=2\\r\\n\\r\\nc=3\\r\\nd=4\\r\\n\\r\\n", "a=\r\n",
				"a=1\r\nb=2","a=1"})
		void testCookieListConstructorWrong(String testStr)
				throws NullPointerException, ValidationException, IOException {

			InputStream testIn = new ByteArrayInputStream(testStr.getBytes());
			MessageInput messageInput = new MessageInput(testIn);
			Throwable t = assertThrows(ValidationException.class, () -> new CookieList(messageInput));
			assertNotNull(t.getMessage());

		}

		// "\r\n\r\n"
		@DisplayName("CookieList(MessageInput) right ")
		@ParameterizedTest()
		@ValueSource(strings = { "\r\nb=2\r\n\r\nc=3\r\nd=4\r\n\r\n", })
		void testCookieListConstructorRightMore(String testStr) throws ValidationException, IOException {

			InputStream testIn = new ByteArrayInputStream(testStr.getBytes());
			MessageInput messageInput = new MessageInput(testIn);
			CookieList newCookieL = new CookieList(messageInput);
			CookieList newCookieLtest = new CookieList();
			assertEquals(newCookieL.equals(newCookieLtest), true);
			CookieList newCookieL1 = new CookieList(messageInput);
			newCookieL1.add("b", "2");
			assertEquals(newCookieL1, newCookieL1);
		}

		@DisplayName("CookieList(MessageInput) right ")
		@ParameterizedTest()
		@ValueSource(strings = { "\r\n", "\r\n\r\n" })
		void testCookieListConstructorRightMore1(String testStr) throws ValidationException, IOException {

			InputStream testIn = new ByteArrayInputStream(testStr.getBytes());
			MessageInput messageInput = new MessageInput(testIn);
			CookieList newCookieL = new CookieList(messageInput);
			CookieList newCookieLtest = new CookieList();
			assertEquals(newCookieL, newCookieLtest);

		}

		@DisplayName("CookieList(MessageInput) right ")
		@ParameterizedTest()
		@ValueSource(strings = { "a=\r\n\r\nb=1\r\n\r\n" })
		void testCookieListConstructorWrong1Right2(String testStr) throws ValidationException, IOException {

			InputStream testIn = new ByteArrayInputStream(testStr.getBytes());
			MessageInput messageInput = new MessageInput(testIn);
			try {
				 new CookieList(messageInput);
			} catch (ValidationException e) {
				CookieList newCookieLtest = new CookieList(messageInput);
				CookieList testCookie = new CookieList();
				testCookie.add("b", "1");
				assertEquals(testCookie, newCookieLtest);
			}
		}
	}
}
