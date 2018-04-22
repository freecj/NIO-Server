/************************************************
*
* Author: <Jian Cao> & Ruoling wang
* Assignment: <Programe 4 >
* Class: <CSI 4321>
*
************************************************/
package N4M.serialization.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import N4M.serialization.ApplicationEntry;
import N4M.serialization.ErrorCodeType;
import N4M.serialization.N4MException;
import N4M.serialization.N4MMessage;
import N4M.serialization.N4MQuery;
import N4M.serialization.N4MResponse;

/**
*
*/
class N4MTest {

	public static int BYTEMASK = 0xff;
	public static long INTMASK = 0xFFFFFFFFL;
	public static int VERSION = 0X20;
	public static int RESPONSE_FLAG = 0x8;
	public static int QUERY_FLAG = 0;
	public static int VERSION_MASK = 0Xf0;
	public static int ERRCODE_MASK = 0x7;
	public static int VERSION_SHIFT = 8;
	public static byte[] queryArray;
	public static byte[] queryWrongArray;
	public static byte[] responseArray;
	public static byte[] responseWrongArrayTime0;
	public static byte[] responseWrongArrayCount0;
	public static String bussName = "123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789";
	public int SHORTMASK = 0xffff;

	// byte for every test
	public byte[] msgResponse;
	public byte[] msgQuery;
	public byte[] msgWrongQuery;

	@BeforeAll
	public static void beforeClass() throws IOException, NullPointerException, N4MException {
		queryMsg();
		responseMsg();
		queryWrongMsg();
		queryWrongResponse();
	}

	// initial the test byte[]
	@BeforeEach
	public void getNewByte() {
		msgResponse = new byte[responseArray.length];
		System.arraycopy(responseArray, 0, msgResponse, 0, responseArray.length);

		msgQuery = new byte[queryArray.length];
		System.arraycopy(queryArray, 0, msgQuery, 0, queryArray.length);

		msgWrongQuery = new byte[queryWrongArray.length];
		System.arraycopy(queryWrongArray, 0, msgWrongQuery, 0, queryWrongArray.length);
	}

	// initial the query test byte[]
	public static void queryMsg() throws IOException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(buf);
		int header = VERSION;
		header |= QUERY_FLAG;
		header <<= VERSION_SHIFT;
		header |= 126 & BYTEMASK;
		String businessName = "123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789";
		out.writeShort(header);
		out.writeByte(businessName.length());
		out.writeBytes(businessName);
		out.flush();
		queryArray = buf.toByteArray();
	}

	// initial the short query test byte[]
	public static void queryWrongMsg() throws IOException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(buf);
		int header = VERSION;
		header |= QUERY_FLAG;
		header <<= VERSION_SHIFT;
		header |= 126 & BYTEMASK;
		out.writeShort(header);
		out.flush();
		queryWrongArray = buf.toByteArray();
	}

	// initial the time stamp and application count = 0 response test byte[]
	public static void queryWrongResponse() throws IOException, NullPointerException, N4MException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		ByteArrayOutputStream buf1 = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(buf);
		DataOutputStream out1 = new DataOutputStream(buf1);
		int header = VERSION;
		header |= RESPONSE_FLAG;
		header <<= VERSION_SHIFT;
		header |= 126 & BYTEMASK;

		Date date = new Date();
		// System.out.println(date);
		List<ApplicationEntry> applications = new ArrayList<ApplicationEntry>();

		out.writeShort(header);
		out.writeInt((int) (date.getTime() / 1000L));
		out.writeByte(applications.size());

		out.flush();

		responseWrongArrayCount0 = buf.toByteArray();

		out1.writeShort(header);
		out1.writeInt((int) (0));
		out1.writeByte(applications.size());
		for (ApplicationEntry entry : applications) {
			out1.writeShort(entry.getAccessCount());
			out1.writeByte(entry.getApplicationName().length());
			out1.writeBytes(entry.getApplicationName());
		}
		out1.flush();
		responseWrongArrayTime0 = buf.toByteArray();
	}

	// initial the response test byte[]
	public static void responseMsg() throws IOException, NullPointerException, N4MException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(buf);
		int header = VERSION;
		header |= RESPONSE_FLAG;
		header <<= VERSION_SHIFT;
		header |= 126 & BYTEMASK;

		Date date = new Date();
		// System.out.println(date);
		List<ApplicationEntry> applications = new ArrayList<ApplicationEntry>();
		for (int i = 0; i < 100; ++i) {
			ApplicationEntry entry = new ApplicationEntry(String.valueOf(i), 500);
			applications.add(entry);
		}

		out.writeShort(header);
		out.writeInt((int) (date.getTime() / 1000L));
		out.writeByte(applications.size());
		for (ApplicationEntry entry : applications) {
			out.writeShort(entry.getAccessCount());
			out.writeByte(entry.getApplicationName().length());
			out.writeBytes(entry.getApplicationName());
		}
		out.flush();
		responseArray = buf.toByteArray();
	}

	@DisplayName("N4MQuery test")
	@Nested
	class N4MQueryTest {

		@DisplayName("N4MQuery equals() right ")
		@Test
		void testN4MQueryEqualsRight() throws IOException, NullPointerException, N4MException {
			N4MMessage test1 = N4MMessage.decode(msgQuery);
			N4MQuery test2 = new N4MQuery(126, bussName);
			if (test1 instanceof N4MQuery) {

				assertAll("euqals", () -> assertEquals(test2, test1),
						() -> assertEquals(test2.toString(), test1.toString()),
						() -> assertEquals(test2.hashCode(), test1.hashCode()),
						() -> assertEquals(((N4MQuery) test2).getBusinessName(), ((N4MQuery) test1).getBusinessName()),
						() -> assertNotEquals(ErrorCodeType.valueOf(0), ErrorCodeType.valueOf(2))

						, () -> {
							assertThrows(N4MException.class, () -> ErrorCodeType.valueOf(5));
						});

			} else {
				fail("fail");
			}

		}

		@DisplayName("N4MQuery too short message ")
		@Test
		void testN4MQueryShort() throws IOException, NullPointerException, N4MException {
			assertThrows(N4MException.class, () -> N4MMessage.decode(msgWrongQuery));
		}

		@DisplayName("N4MQuery too long message ")
		@Test
		void testN4MQueryLong() throws IOException, NullPointerException, N4MException {
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(buf);
			int header = VERSION;
			header |= QUERY_FLAG;
			header <<= VERSION_SHIFT;
			header |= 126 & BYTEMASK;
			String businessName = "123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789";
			out.writeShort(header);
			out.writeByte(businessName.length());
			out.writeBytes(businessName);
			out.writeBytes(businessName);
			out.flush();
			assertThrows(N4MException.class, () -> N4MMessage.decode(buf.toByteArray()));
		}

		@DisplayName("N4MQuery equals() not ")
		@Test
		void testN4MQueryNotEqualsRight() throws IOException, NullPointerException, N4MException {

			N4MMessage test1 = N4MMessage.decode(msgQuery);
			N4MQuery test2 = new N4MQuery(126, bussName + '1');
			if (test1 instanceof N4MQuery) {
				assertNotEquals(test2, test1);
			} else {
				fail("fail");
			}

		}

		@DisplayName("N4MQuery setBusinessName() NullPointerException ")
		@Test
		void querySetBuzNameNull() throws IOException, N4MException, NullPointerException {
			N4MQuery test2 = new N4MQuery(126, "aa");
			Throwable t = assertThrows(NullPointerException.class, () -> test2.setBusinessName(null));
			assertNotNull(t.getMessage());
		}

		@DisplayName("N4MQuery setBusinessName() N4MException ASCII")
		@Test
		void querySetBuzNameN4MException() throws IOException, NullPointerException, N4MException {
			N4MQuery test2 = new N4MQuery(126, "1");
			assertThrows(N4MException.class, () -> test2.setBusinessName("我们"));
		}

		@DisplayName("N4MQuery setBusinessName() N4MException LENGTH")
		@Test
		void querySetBuzNameN4MExceptionLong() throws IOException, NullPointerException, N4MException {
			String maxstring = "";
			for (int i = 0; i < BYTEMASK + 1; ++i) {
				maxstring += "2";
			}
			String teString = maxstring;
			N4MQuery test2 = new N4MQuery(126, "1");
			assertThrows(N4MException.class, () -> test2.setBusinessName(teString));
		}

		@DisplayName("N4MQuery encode() ")
		@Test
		void queryEncode() throws IOException, NullPointerException, N4MException {
			N4MMessage test1 = N4MMessage.decode(msgQuery);
			if (test1 instanceof N4MQuery) {

				assertArrayEquals(msgQuery, test1.encode());
			} else {
				fail("fail");
			}
		}

		@DisplayName("N4MQuery setErrorCodeNum() exception")
		@ParameterizedTest(name = "{index} ==> errCodeNum=''{0}''")
		@ValueSource(ints = { 1, 2, 3, 4, 5, -1 })
		void setErrorCodeTest(int args) throws IOException, NullPointerException, N4MException {
			N4MMessage test1 = N4MMessage.decode(msgQuery);
			if (test1 instanceof N4MQuery) {
				N4MException t = assertThrows(N4MException.class,
						() -> test1.setErrorCode(ErrorCodeType.valueOf(args)));
				assertEquals(ErrorCodeType.INCORRECTHEADER, t.getErrorCodeType());
			} else {
				fail("fail");
			}
		}

	}

	@DisplayName("N4MResponse test")
	@Nested
	class N4MResponseTest {

		@DisplayName("N4MResponse encode() ")
		@Test
		void responseEncode() throws IOException, NullPointerException, N4MException {

			N4MMessage test1 = N4MMessage.decode(msgResponse);
			N4MMessage test2 = N4MMessage.decode(msgResponse);
			N4MMessage test3 = N4MMessage.decode(msgResponse);
			 new N4MResponse(((N4MResponse)test1).getErrorCode(), ((N4MResponse)test1).getMsgId(), ((N4MResponse)test1).getTimestamp(),
					((N4MResponse)test1).getApplications()
					);
			if (test1 instanceof N4MResponse) {

				assertAll("properties", () -> assertArrayEquals(responseArray, test1.encode()),
						() -> assertEquals(test1.toString(), test2.toString()), () -> assertEquals(test1, test2),
						() -> assertEquals(test1.getMsgId(), test2.getMsgId()),
						() -> assertEquals(((N4MResponse) test3).getApplications(),
								((N4MResponse) test2).getApplications()),

						() -> {
							
							((N4MResponse) test3).setTimestamp(5000);
							((N4MResponse) test3).setTimestamp(10000);

							assertAll("setTimestamp modify",
									() -> assertNotEquals(((N4MResponse) test3).hashCode(),
											((N4MResponse) test2).hashCode()),
									() -> assertNotEquals(((N4MResponse) test3).getTimestamp(),
											((N4MResponse) test2).getTimestamp()));
						}, () -> {
							// test ApplicationEntry the normal set
							List<ApplicationEntry> testList = ((N4MResponse) test3).getApplications();
							testList.forEach((u) -> {
								try {
									u.setApplicationName(u.getApplicationName());
								} catch (NullPointerException | N4MException e) {

								}
							});
							assertEquals(((N4MResponse) test3).getApplications(),
									((N4MResponse) test2).getApplications());
						}, () -> {
							// test ApplicationEntry copy
							List<ApplicationEntry> testList = ((N4MResponse) test3).getApplications();
							testList.forEach((u) -> {
								try {
									u.setApplicationName("222");
								} catch (NullPointerException | N4MException e) {

								}
							});
							testList.clear();
							assertAll("applications",
									() -> assertEquals(((N4MResponse) test3).getApplications(),
											((N4MResponse) test2).getApplications()),
									() -> assertNotEquals(test3, test2));
						});

			} else {
				fail("fail");
			}
		}

		@DisplayName("N4MResponse decode header wrong ")
		@Test
		void decodeHeader() throws IOException, NullPointerException, N4MException {

			msgResponse[0] = 0x2;
			N4MException t = assertThrows(N4MException.class, () -> N4MMessage.decode(msgResponse));
			assertEquals(ErrorCodeType.INCORRECTHEADER, t.getErrorCodeType());
		}

		@DisplayName("N4MResponse decode ErroCode wrong ")
		@Test
		void decodeErroCode() throws IOException, NullPointerException, N4MException {

			msgResponse[0] = 0x2f;
			N4MException t = assertThrows(N4MException.class, () -> N4MMessage.decode(msgResponse));
			assertEquals(ErrorCodeType.INCORRECTHEADER, t.getErrorCodeType());
		}

		@DisplayName("N4MResponse application entry access out of the range ")
		@Test
		void access() throws IOException, NullPointerException, N4MException {
			N4MMessage test1 = N4MMessage.decode(msgResponse);
			((N4MResponse) test1).getApplications()
					.forEach((u) -> assertThrows(N4MException.class, () -> u.setAccessCount(SHORTMASK + 1)));
		}

		@DisplayName("N4MResponse timestamp out of the range ")
		@Test
		void timestampTest() throws IOException, NullPointerException, N4MException {

			N4MMessage test1 = N4MMessage.decode(msgResponse);
			N4MException t = assertThrows(N4MException.class,
					() -> ((N4MResponse) test1).setTimestamp( (INTMASK + 1L)));
			assertEquals(ErrorCodeType.BADMSG, t.getErrorCodeType());
		}

		@DisplayName("N4MResponse msgid out of the range ")
		@Test
		void msgidTest() throws IOException, NullPointerException, N4MException {

			N4MMessage test1 = N4MMessage.decode(msgResponse);
			N4MException t = assertThrows(N4MException.class, () -> test1.setMsgId(BYTEMASK + 1));
			assertEquals(ErrorCodeType.BADMSG, t.getErrorCodeType());
		}

		@DisplayName("N4MResponse setApplicationName out of the range ")
		@Test
		void AppNameTest() throws IOException, NullPointerException, N4MException {

			N4MMessage test1 = N4MMessage.decode(msgResponse);
			List<ApplicationEntry> testList = ((N4MResponse) test1).getApplications();
			String testString = "";
			for (int i = 0; i < BYTEMASK + 1; i++) {
				testString += 1;
			}

			String temp = testString;
			for (ApplicationEntry entry : testList) {
				N4MException t = assertThrows(N4MException.class, () -> entry.setApplicationName(temp));
				assertEquals(ErrorCodeType.BADMSG, t.getErrorCodeType());
			}
		}

		@DisplayName("N4MResponse setApplicationName Not ASCII ")
		@Test
		void AppNameASCIITest() throws IOException, NullPointerException, N4MException {

			N4MMessage test1 = N4MMessage.decode(msgResponse);
			List<ApplicationEntry> testList = ((N4MResponse) test1).getApplications();
			String testString = "";
			for (int i = 0; i < BYTEMASK; i++) {
				testString += "我";
			}

			String temp = testString;
			for (ApplicationEntry entry : testList) {
				N4MException t = assertThrows(N4MException.class, () -> entry.setApplicationName(temp));
				assertEquals(ErrorCodeType.BADMSG, t.getErrorCodeType());
			}
		}

		@DisplayName("N4MResponse setErrorCodeNum() Exception")
		@ParameterizedTest(name = "{index} ==> errCodeNum=''{0}''")
		@ValueSource(ints = { -1, 5, 6 })
		void setErrorCodeMsgException(int argument) throws IOException, NullPointerException, N4MException {
			N4MMessage test1 = N4MMessage.decode(msgResponse);

			N4MException t = assertThrows(N4MException.class, () -> test1.setErrorCode( ErrorCodeType.valueOf(argument)));
			assertEquals(ErrorCodeType.INCORRECTHEADER, t.getErrorCodeType());
			assertNotEquals(-1, test1.getErrorCode());
		}

		@DisplayName("N4MResponse decode byte in is null")
		@Test
		void decodeNull() throws IOException, N4MException {
			assertThrows(NullPointerException.class, () -> N4MMessage.decode(null));
		}

		@DisplayName("N4MResponse decode timestamp = 0 , application !=0 wrong case")
		@Test
		void decodeTime0() throws IOException, N4MException {

			N4MException t = assertThrows(N4MException.class, () -> N4MMessage.decode(responseWrongArrayTime0));
			assertEquals(ErrorCodeType.BADMSG, t.getErrorCodeType());
		}

		@DisplayName("N4MResponse decode application = 0 , timestamp !=0 wrong case")
		@Test
		void decodeCount0() throws IOException, N4MException {

			N4MException t = assertThrows(N4MException.class, () -> N4MMessage.decode(responseWrongArrayCount0));
			assertEquals(ErrorCodeType.BADMSG, t.getErrorCodeType());
		}
	}

}