package test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
	ClientTest.class,
	ReadRequestPacketTest.class,
	WriteRequestPacketTest.class,
	DataPacketTest.class,
	AcknowledgementPacketTest.class,
	ErrorPacketTest.class,
	FileParserReadTest.class,
	FileParserWriteTest.class,
	WriteFileAcceptanceTest.class,
	ReadFileAcceptanceTest.class
})
public class TFTPSuite { }
