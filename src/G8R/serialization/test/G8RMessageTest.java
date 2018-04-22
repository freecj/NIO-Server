/************************************************
*
* Author: <Jian Cao>
* Assignment: <Prgrame 1 >
* Class: <CSI 4321>
*
************************************************/
package G8R.serialization.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import G8R.serialization.CookieList;
import G8R.serialization.G8RMessage;
import G8R.serialization.G8RResponse;
import G8R.serialization.MessageInput;
import G8R.serialization.MessageOutput;
import G8R.serialization.ValidationException;
import G8R.serialization.G8RRequest;

/**
 * G8RMessageTest test G8RMessage class.
 */
class G8RMessageTest {
	@DisplayName("G8RRequest test")
	@Nested
	/**
	 * request message class.
	 */
	class G8RRequestest {
		/**
		 * G8RRequest class setFunction() right case
		 */
		@DisplayName("G8RRequest setfunction() right ")
		@Test
		void testRequestSetFunctionRight() throws ValidationException, IOException {

			String testStr = "G8R/1.0 Q RUN aa 11 Xsadsad ccvvv sfdsf\r\na=1\r\nb=2\r\n\r\n";
			InputStream testIn;

			testIn = new ByteArrayInputStream(testStr.getBytes("ASCII"));
			MessageInput messageInput = new MessageInput(testIn);
			G8RMessage test = G8RMessage.decode(messageInput);
			if (test instanceof G8RRequest) {
				assertEquals("aa", ((G8RRequest) test).getFunction());
			} else {
				fail("fail");
			}
		}

		/**
		 * G8RRequest class setFunction() nullpointer wrong case
		 */
		@DisplayName("G8RRequest setfunction() wrong nullpointer")
		@Test
		void testRequestSetFunctioNull() throws ValidationException, IOException {

			String testStr = "G8R/1.0 Q RUN aa 11 Xsadsad ccvvv sfdsf\r\na=1\r\nb=2\r\n\r\n";
			InputStream testIn;

			testIn = new ByteArrayInputStream(testStr.getBytes("ASCII"));
			MessageInput messageInput = new MessageInput(testIn);
			G8RMessage test = G8RMessage.decode(messageInput);
			if (test instanceof G8RRequest) {
				Throwable t = assertThrows(NullPointerException.class, () -> ((G8RRequest) test).setFunction(null));
				assertNotNull(t.getMessage());
			} else {
				fail("fail");
			}
		}

		/**
		 * G8RRequest class setFunction() throw ValidationException, set a invalid string
		 */
		@DisplayName("G8RRequest setfunction() wrong validtion")
		@Test
		void testRequestSetFunctioValidition() throws ValidationException, IOException {

			String testStr = "G8R/1.0 Q RUN aa 11 Xsadsad ccvvv sfdsf\r\na=1\r\nb=2\r\n\r\n";
			InputStream testIn;

			testIn = new ByteArrayInputStream(testStr.getBytes("ASCII"));
			MessageInput messageInput = new MessageInput(testIn);
			G8RMessage test = G8RMessage.decode(messageInput);
			if (test instanceof G8RRequest) {
				Throwable t = assertThrows(ValidationException.class, () -> ((G8RRequest) test).setFunction("a."));
				assertNotNull(t.getMessage());
			} else {
				fail("fail");
			}
		}

		/**
		 * G8RRequest class getCookieList() right case
		 */
		@DisplayName("G8RRequest getCookieList() right case")
		@Test
		void testRequestGetCookieList() throws ValidationException, IOException {

			String testStr = "G8R/1.0 Q RUN aa 11 Xsadsad ccvvv sfdsf\r\na=1\r\nb=2\r\n\r\n";
			InputStream testIn;

			testIn = new ByteArrayInputStream(testStr.getBytes("ASCII"));
			MessageInput messageInput = new MessageInput(testIn);
			G8RMessage test = G8RMessage.decode(messageInput);
			if (test instanceof G8RRequest) {
				CookieList testCookie = ((G8RRequest) test).getCookieList();
				assertEquals("Cookies=[a=1,b=2]", testCookie.toString());
			} else {
				fail("fail");
			}
		}

		/**
		 * G8RRequest class getparam() right case: test whether is a deep copy
		 */
		@DisplayName("G8RRequest getparam() right case")
		@Test
		void testRequestGetParamValidition() throws ValidationException, IOException {

			String testStr = "G8R/1.0 Q RUN aa 11 Xsadsad ccvvv sfdsf\r\na=1\r\nb=2\r\n\r\n";
			InputStream testIn;

			testIn = new ByteArrayInputStream(testStr.getBytes("ASCII"));
			MessageInput messageInput = new MessageInput(testIn);
			G8RMessage test = G8RMessage.decode(messageInput);
			if (test instanceof G8RRequest) {
				String[] paramStr = ((G8RRequest) test).getParams();

				assertNotEquals(paramStr, ((G8RRequest) test).getParams());
				paramStr = null;
				assertNotNull(((G8RRequest) test).getParams());

			} else {
				fail("fail");
			}
		}

		/**
		 * G8RRequest class Setparam() throw NullPointerException, parameters is null
		 */
		@DisplayName("G8RRequest Setparam() NullPointerException")
		@Test
		void testRequestSetParamNull() throws ValidationException, IOException {

			String testStr = "G8R/1.0 Q RUN aa 11 Xsadsad ccvvv sfdsf\r\na=1\r\nb=2\r\n\r\n";
			InputStream testIn;
			String[] paramTest = new String[3];
			paramTest[0] = "abc";
			paramTest[1] = "c";
			testIn = new ByteArrayInputStream(testStr.getBytes("ASCII"));
			MessageInput messageInput = new MessageInput(testIn);
			G8RMessage test = G8RMessage.decode(messageInput);
			if (test instanceof G8RRequest) {
				Throwable t = assertThrows(NullPointerException.class, () -> ((G8RRequest) test).setParams(paramTest));
				assertNotNull(t.getMessage());
			} else {
				fail("fail");
			}
		}

		/**
		 * G8RRequest class Setparam() throw ValidationException, parameters is invalid
		 */
		@DisplayName("G8RRequest Setparam() ValidationException")
		@Test
		void testRequestSetParamValid() throws ValidationException, IOException {

			String testStr = "G8R/1.0 Q RUN aa 11 Xsadsad ccvvv sfdsf\r\na=1\r\nb=2\r\n\r\n";
			InputStream testIn;
			String[] paramTest = new String[3];
			paramTest[0] = "abc";
			paramTest[1] = "c....";
			paramTest[2] = "xx";
			testIn = new ByteArrayInputStream(testStr.getBytes("ASCII"));
			MessageInput messageInput = new MessageInput(testIn);
			G8RMessage test = G8RMessage.decode(messageInput);
			if (test instanceof G8RRequest) {
				Throwable t = assertThrows(ValidationException.class, () -> ((G8RRequest) test).setParams(paramTest));
				assertNotNull(t.getMessage());
			} else {
				fail("fail");
			}
		}

		/**
		 * G8RRequest class Setparam() modify, see if is a deep copy or not .
		 */
		@DisplayName("G8RRequest Setparam() modify")
		@Test
		void testRequestSetParamClear() throws ValidationException, IOException {

			String testStr = "G8R/1.0 Q RUN aa 11 Xsadsad ccvvv sfdsf\r\na=1\r\nb=2\r\n\r\n";
			InputStream testIn;
			String[] paramTest = new String[3];
			paramTest[0] = "abc";
			paramTest[1] = "sdsd";
			paramTest[2] = "xx";
			testIn = new ByteArrayInputStream(testStr.getBytes("ASCII"));
			MessageInput messageInput = new MessageInput(testIn);
			G8RMessage test = G8RMessage.decode(messageInput);

			if (test instanceof G8RRequest) {
				((G8RRequest) test).setParams(paramTest);
				paramTest[1] = ".....";
				assertNotEquals(paramTest[1], ((G8RRequest) test).getParams()[1]);
			} else {
				fail("fail");
			}
		}

		/**
		 * G8RRequest class Encode() right case
		 */
		@DisplayName("G8RRequest Encode() right ")
		@Test
		void testRequestEncode() throws ValidationException, IOException {

			String testStr = "G8R/1.0 Q RUN aa 11 Xsadsad ccvvv sfdsf\r\na=1\r\nb=2\r\n\r\n";
			InputStream testIn;
			String[] paramTest = new String[3];
			paramTest[0] = "abc";
			paramTest[1] = "sdsd";
			paramTest[2] = "xx";
			testIn = new ByteArrayInputStream(testStr.getBytes("ASCII"));
			MessageInput messageInput = new MessageInput(testIn);
			G8RMessage test = G8RMessage.decode(messageInput);
			OutputStream out = new ByteArrayOutputStream();
			MessageOutput messageOutput = new MessageOutput(out);
			if (test instanceof G8RRequest) {
				test.encode(messageOutput);
				String data = out.toString();
				assertEquals(testStr, data);
				if (test instanceof G8RRequest) {
					((G8RRequest) test).setParams(paramTest);
					OutputStream outModify = new ByteArrayOutputStream();
					MessageOutput msgOutModify = new MessageOutput(outModify);
					test.encode(msgOutModify);
					data = outModify.toString();

					assertEquals("G8R/1.0 Q RUN aa abc sdsd xx\r\na=1\r\nb=2\r\n\r\n", data);

				} else {
					fail("fail");
				}

			} else {
				fail("fail");
			}
		}

		/**
		 * G8RRequest class Tostring() right case
		 */
		@DisplayName("G8RRequest Tostring() right case")
		@Test
		void testRequestTostring() throws ValidationException, IOException {

			String testStr = "G8R/1.0 Q RUN aa 11 Xsadsad ccvvv sfdsf\r\na=1\r\nb=2\r\n\r\n";
			InputStream testIn;

			testIn = new ByteArrayInputStream(testStr.getBytes("ASCII"));
			MessageInput messageInput = new MessageInput(testIn);
			G8RMessage test = G8RMessage.decode(messageInput);
			if (test instanceof G8RRequest) {
				assertEquals("request: aa 11 Xsadsad ccvvv sfdsf\r\nCookies=[a=1,b=2]", ((G8RRequest) test).toString());
			} else {
				fail("fail");
			}
		}

		/**
		 * test the situation that validation happens when using the Constructor with input parameter
		 * MessageInput in.
		 * 1. request and response validation on every segments.
		 *
		 */
		@DisplayName("G8RRequest(MessageInput) wrong ")
		@ParameterizedTest(name = "{index} => str=''{0}''")
		@ValueSource(strings = { "G8R/1.0 Q RUN  11\r\na=1\r\nb=2\r\n\r\n",
				"G8R/1.0 Q RUN a....a11Xsadsadccvvvsfdsf\r\na=1\r\nb=2\r\n\r\n",
				"G8R/1.0 Q RUN a ....a11Xsadsadccvvvsfdsf\r\na=1\r\nb=2\r\n\r\n", "\r\na=1\r\nb=2\r\n\r\n", "\r\n",
				"G8R/1.0 Q RUN a ....a11Xsadsadccvvvsfdsf\r\na=1\r\nb=2\r\n\r\n",
				"G8R/1.0 XX RUN a ....a11Xsadsadccvvvsfdsf\r\na=1\r\nb=2\r\n\r\n", "G11 ", "G11  ",
				"G8R/1.0 Q VVVVVV555!!! a ....a11Xsadsadccvvvsfdsf\r\na=1\r\nb=2\r\n\r\n",
				"G8R/1.0 Q RUN a AD..SA ..a11Xsadsadccvvvsfdsf\r\na=1\r\nb=2\r\n\r\n",
				"G8R/1.0 R XXX aa asdasdasadsadax0\r\n\r\n", "G8R/1.0 R OK ... asdasdasadsadax0\r\n\r\n", "G8R/1.0 ",
				"G8R/1.0 R ", "G8R/1.0 R OK ", "G8R/1.0 R OK ad ", "G8R/1.0 Q " })
		void testDecodeWrong(String testStr) throws ValidationException, IOException {

			InputStream testIn;
			testIn = new ByteArrayInputStream(testStr.getBytes("ASCII"));
			MessageInput messageInput = new MessageInput(testIn);
			Throwable t = assertThrows(ValidationException.class, () -> G8RMessage.decode(messageInput));
			assertNotNull(t.getMessage());
		}

		/**
		 * test the situation that validation happens when using the Constructor with input parameter
		 * MessageInput in.
		 * Right case
		 */
		@DisplayName("G8RRequest(MessageInput) right case ")
		@ParameterizedTest(name = "{index} => str=''{0}''")
		@ValueSource(strings = { "G8R/1.0 Q RUN f\r\na=1\r\nb=2\r\n\r\n",

		})
		void testDecodeRight(String testStr) throws ValidationException, IOException {

			InputStream testIn;

			testIn = new ByteArrayInputStream(testStr.getBytes("ASCII"));
			MessageInput messageInput = new MessageInput(testIn);
			G8RMessage test = G8RMessage.decode(messageInput);
			if (test instanceof G8RRequest) {

				OutputStream outModify = new ByteArrayOutputStream();
				MessageOutput msgOutModify = new MessageOutput(outModify);
				test.encode(msgOutModify);
				String data = outModify.toString();
				assertEquals(testStr, data);
			} else {
				fail("wrong");
			}
		}

		/**
		 * test the situation that validation happens when using the Constructor with input parameter
		 * MessageInput in.
		 * empty string, throw EOF 
		 */
		@DisplayName("G8RRequest(MessageInput) empty string ")
		@ParameterizedTest()
		@ValueSource(strings = { "" })
		void testDecodeWrongEmpty(String testStr) throws ValidationException, IOException {

			InputStream testIn;

			testIn = new ByteArrayInputStream(testStr.getBytes("ASCII"));
			MessageInput messageInput = new MessageInput(testIn);
			Throwable t = assertThrows(EOFException.class, () -> G8RMessage.decode(messageInput));
			assertNotNull(t.getMessage());

		}

		/**
		 * requset case right case.
		 */
		@Test
		void testRequest() throws ValidationException, IOException {

			String testStr = "G8R/1.0 Q RUN aa 11 Xsadsad ccvvv sfdsf\r\na=1\r\nb=2\r\n\r\n";
			InputStream testIn = new ByteArrayInputStream(testStr.getBytes("ASCII"));
			MessageInput messageInput = new MessageInput(testIn);
			G8RMessage temp = G8RMessage.decode(messageInput);
			OutputStream out = new ByteArrayOutputStream();
			MessageOutput messageOutput = new MessageOutput(out);
			if (temp instanceof G8RRequest) {
				temp.encode(messageOutput);
				String data = out.toString();
				assertEquals(testStr, data);
			} else {
				fail("testrequest");
			}
		}

	}

	/**
	 * G8RResponse case right case.
	 */
	@DisplayName("G8RResponse right case")
	@Nested
	class G8RResponsetest {
		@DisplayName("getCookieList() right case")
		@Test
		void testGetCookieList() throws ValidationException, IOException {

			String testStr = "G8R/1.0 R OK aa asdasdasadsadax0\r\n\r\na=1\r\nb=2\r\n\r\n";
			InputStream testIn;

			testIn = new ByteArrayInputStream(testStr.getBytes("ASCII"));
			MessageInput messageInput = new MessageInput(testIn);
			G8RMessage test = G8RMessage.decode(messageInput);
			if (test instanceof G8RResponse) {
				CookieList testCookie = ((G8RResponse) test).getCookieList();
				assertEquals("Cookies=[]", testCookie.toString());
			} else {
				fail("fail");
			}
		}

		/**
		 * G8RResponse Encode right case.
		 */
		@DisplayName(" Encode() right")
		@Test
		void testResponseEncode() throws ValidationException, IOException {

			String testStr = "G8R/1.0 R OK aa asdasdasadsadax0\r\na=1\r\nb=2\r\n\r\n";
			InputStream testIn;

			testIn = new ByteArrayInputStream(testStr.getBytes("ASCII"));
			MessageInput messageInput = new MessageInput(testIn);
			G8RMessage test = G8RMessage.decode(messageInput);
			OutputStream out = new ByteArrayOutputStream();
			MessageOutput messageOutput = new MessageOutput(out);
			if (test instanceof G8RResponse) {
				test.encode(messageOutput);
				String data = out.toString();
				assertEquals(testStr, data);

			} else {
				fail("fail");
			}
		}

		/**
		 * decode function test Valid Response Message
		 */
		@DisplayName("G8RResponse decode (MessageInput) right ")
		@Test
		void testDecodeRight() throws ValidationException, IOException {
			char tt = 0x7e;
			String teString = "G8R/1.0 R OK aa asdasdasadsadax0" + tt + "\r\n\r\n";

			InputStream testIn = new ByteArrayInputStream(teString.getBytes("ASCII"));
			MessageInput messageInput = new MessageInput(testIn);
			G8RMessage temp = G8RMessage.decode(messageInput);
			OutputStream out = new ByteArrayOutputStream();
			MessageOutput messageOutput = new MessageOutput(out);
			if (temp instanceof G8RResponse) {
				temp.encode(messageOutput);
				String data = out.toString();
				assertEquals(teString, data);
			} else {
				fail("testDecodeRight");
			}
		}

		/**
		 * decode function test invalid for NonPrintable
		 */
		@DisplayName("G8RResponse decode(MessageInput) NonPrintable ")
		@Test
		void testDecodeNonPrintable() throws ValidationException, IOException {
			char tt = 0x7f;
			String teString = "G8R/1.0 R OK aa asdasdasadsadax0" + tt + "\r\n\r\n";

			InputStream testIn = new ByteArrayInputStream(teString.getBytes("ASCII"));
			MessageInput messageInput = new MessageInput(testIn);

			Throwable t = assertThrows(ValidationException.class, () -> G8RMessage.decode(messageInput));
			assertNotNull(t.getMessage());
		}

		/**
		 * G8RResponse setfunction() test invalid for nullpointer
		 */
		@DisplayName("G8RResponse setfunction() wrong nullpointer")
		@Test
		void testResponseSetFunctioNull() throws ValidationException, IOException {

			String testStr = "G8R/1.0 R OK aa asdasdasadsadax0\r\n\r\n";
			InputStream testIn;

			testIn = new ByteArrayInputStream(testStr.getBytes("ASCII"));
			MessageInput messageInput = new MessageInput(testIn);
			G8RMessage test = G8RMessage.decode(messageInput);
			if (test instanceof G8RResponse) {
				Throwable t = assertThrows(NullPointerException.class, () -> ((G8RResponse) test).setFunction(null));
				assertNotNull(t.getMessage());
			} else {
				fail("fail");
			}
		}

		/**
		 * G8RResponse setStatus() invalid 
		 */
		@DisplayName("G8RResponse setStatus() validation wrong")
		@ParameterizedTest(name = "{index} => str=''{0}''")
		@ValueSource(strings = { "G8R/1.0 R OK aa asdasdasadsadax0\r\n\r\n",
				"G8R/1.0 R ERROR aa asdasdasadsadax0\r\n\r\n" })
		void testSetStatusValidok(String testStr) throws ValidationException, IOException {

			InputStream testIn = new ByteArrayInputStream(testStr.getBytes("ASCII"));
			MessageInput messageInput = new MessageInput(testIn);
			G8RMessage test = G8RMessage.decode(messageInput);
			if (test instanceof G8RResponse) {
				Throwable t = assertThrows(ValidationException.class, () -> ((G8RResponse) test).setStatus("error"));
				assertNotNull(t.getMessage());

			} else {
				fail("fail");
			}
		}

		/**
		 * G8RResponse setStatus() samll "ok" throw ValidationException
		 */
		@DisplayName("G8RResponse setStatus() validation")
		@ParameterizedTest(name = "{index} => str=''{0}''")
		@ValueSource(strings = { "G8R/1.0 R OK aa asdasdasadsadax0\r\n\r\n",
				"G8R/1.0 R ERROR aa asdasdasadsadax0\r\n\r\n" })
		void testSetStatusValiderror(String testStr) throws ValidationException, IOException {

			InputStream testIn = new ByteArrayInputStream(testStr.getBytes("ASCII"));
			MessageInput messageInput = new MessageInput(testIn);
			G8RMessage test = G8RMessage.decode(messageInput);
			if (test instanceof G8RResponse) {
				Throwable t = assertThrows(ValidationException.class, () -> ((G8RResponse) test).setStatus("ok"));
				assertNotNull(t.getMessage());

			} else {
				fail("fail");
			}
		}

		/**
		 * G8RResponse setfunction() right case
		 */
		@DisplayName("G8RResponse setfunction() right")
		@Test
		void testResponseGetStatusNull() throws ValidationException, IOException {

			String testStr = "G8R/1.0 R OK aa asdasdasadsadax0\r\n\r\n";
			InputStream testIn;

			testIn = new ByteArrayInputStream(testStr.getBytes("ASCII"));
			MessageInput messageInput = new MessageInput(testIn);
			G8RMessage test = G8RMessage.decode(messageInput);
			if (test instanceof G8RResponse) {
				assertEquals("OK", ((G8RResponse) test).getStatus());

			} else {
				fail("fail");
			}
		}

		/**
		 * G8RResponse setfunction() wrong case for invalid character
		 */
		@DisplayName("G8RResponse setfunction() validation")
		@Test
		void testResponseSetFunctionValid() throws ValidationException, IOException {

			String testStr = "G8R/1.0 R OK aa asdasdasadsadax0\r\n\r\n";
			InputStream testIn;

			testIn = new ByteArrayInputStream(testStr.getBytes("ASCII"));
			MessageInput messageInput = new MessageInput(testIn);
			G8RMessage test = G8RMessage.decode(messageInput);
			if (test instanceof G8RResponse) {
				Throwable t = assertThrows(ValidationException.class, () -> ((G8RResponse) test).setFunction("@@@@"));
				assertNotNull(t.getMessage());
			} else {
				fail("fail");
			}
		}

		/**
		 * G8RResponse setMessage() wrong case for invalid character
		 */
		@DisplayName("G8RResponse setMessage() validation")
		@Test
		void testResponseSetMessageValid() throws ValidationException, IOException {

			String testStr = "G8R/1.0 R OK aa asdasdasadsadax0\r\n\r\n";
			InputStream testIn;

			testIn = new ByteArrayInputStream(testStr.getBytes("ASCII"));
			MessageInput messageInput = new MessageInput(testIn);
			G8RMessage test = G8RMessage.decode(messageInput);
			if (test instanceof G8RResponse) {
				Throwable t = assertThrows(ValidationException.class, () -> ((G8RResponse) test).setMessage("\n\r"));
				assertNotNull(t.getMessage());
			} else {
				fail("fail");
			}
		}

		/**
		 * test G8RResponse Tostring() right case
		 */
		@DisplayName("G8RResponse Tostring() test case")
		@Test
		void testTostring() throws ValidationException, IOException {

			String testStr = "G8R/1.0 R OK aa asdasdasadsadax0\r\n\r\n";
			InputStream testIn;

			testIn = new ByteArrayInputStream(testStr.getBytes("ASCII"));
			MessageInput messageInput = new MessageInput(testIn);
			G8RMessage test = G8RMessage.decode(messageInput);
			if (test instanceof G8RResponse) {
				assertAll("String",
						() -> assertEquals("response: OK aa asdasdasadsadax0\r\nCookies=[]",
								((G8RResponse) test).toString()),
						() -> assertEquals("asdasdasadsadax0", ((G8RResponse) test).getMessage()),
						() -> assertEquals("aa", ((G8RResponse) test).getFunction()));

			} else {
				fail("fail");
			}
		}

	}

}
