package main;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import com.huawei.iota.iodev.datatrans.DataTransService;
import gnu.io.*;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;

public class ContinueRead extends Thread implements SerialPortEventListener { // SerialPortEventListener
    static CommPortIdentifier portId; // ����ͨ�Ź�����
    static Enumeration<?> portList; // ��Ч�����ϵĶ˿ڵ�ö��
    InputStream inputStream; // �Ӵ�������������
    OutputStream outputStream;// �򴮿��������
    static SerialPort serialPort; // ���ڵ�����

    private static int calls = 0;

    private synchronized void onDataAvailable() {
        byte[] readBuffer = new byte[255];
        try {
            calls ++;
            if (calls != 1) {
                System.out.println("Error! Reenter...");
            }
            int numBytes = -1;
            int len = 0;
            int ID = 0;
            while (inputStream.available() > 0) {
                numBytes = inputStream.read(readBuffer, 0, 1);
                if (numBytes > 0) {
                    if(readBuffer[0] == 0x55) {
                        numBytes = inputStream.read(readBuffer, 1, 1);
                        if(readBuffer[1] == 0x55) {
                            len = inputStream.read();
                            ID = inputStream.read();
                            numBytes = inputStream.read(readBuffer, 0, len - 4);
                            if(numBytes != (len - 4)) {
                                System.out.println("��ȡ���ݳ���");
                            }else {
                                /* ת��ʮ����������Ϊ�ַ������ϱ� */
                                String helperId = String.valueOf(ID>>4);
                                String tagId = ByteToArray(readBuffer,len-4);
                                agentLiteDataTrans.gwDataReport(helperId, tagId);
                                SaveReportInf(String.valueOf(ID>>4)+String.valueOf(ID&0x0F),tagId);
                            }
                        }
                    }
                    readBuffer = new byte[255];// ���¹��컺����󣬷����п��ܻ�Ӱ����������յ�����
                } else {
                    System.out.println("û�ж�������");
                }
            }
        } catch (IOException e) {
            System.out.println(" ============ ���ڳ��� ============ ");
        } finally {
            calls --;
        }
    }

	@Override
	/*
	* SerialPort EventListene �ķ���,���������˿����Ƿ���������
	*/
    public void serialEvent(SerialPortEvent event) {
        switch (event.getEventType()) {
		case SerialPortEvent.BI:
		case SerialPortEvent.OE:
		case SerialPortEvent.FE:
		case SerialPortEvent.PE:
		case SerialPortEvent.CD:
		case SerialPortEvent.CTS:
		case SerialPortEvent.DSR:
		case SerialPortEvent.RI:
		case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
		    break;
        case SerialPortEvent.DATA_AVAILABLE:// ���п�������ʱ��ȡ����
            onDataAvailable();
            break;
        }
    }

	/**
	* 
	* ͨ�������COM���ڣ����ü������Լ���صĲ���
	* 
	* @return ����1 ��ʾ�˿ڴ򿪳ɹ������� 0��ʾ�˿ڴ�ʧ��
	*/
    AgentLiteDataTrans agentLiteDataTrans = AgentLiteDataTrans.getInstance();
    DataTransService dataTransService = DataTransService.getInstance();
    public int startComPort() {
		// ͨ������ͨ�Ź������õ�ǰ�����ϵĴ����б�
		portList = CommPortIdentifier.getPortIdentifiers();
		dataTransService.registerObserver(agentLiteDataTrans);

		while (portList.hasMoreElements()) {

			// ��ȡ��Ӧ���ڶ���
			portId = (CommPortIdentifier) portList.nextElement();

			// �ж϶˿������Ƿ�Ϊ����
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				// �ж�������ڴ��ڣ��ʹ򿪸ô���
				if (portId.getName().equals(AgentLiteBase.COM)) {
					try {
						// �򿪴���,�ӳ�Ϊ2����
						serialPort = (SerialPort) portId.open(AgentLiteBase.COM, 2000);
						
					} catch (PortInUseException e) {
			            e.printStackTrace();
			            return 0;
			        }
			        // ���õ�ǰ���ڵ����������
			        try {
			            inputStream = serialPort.getInputStream();
			            outputStream = serialPort.getOutputStream();
			        } catch (IOException e) {
			            e.printStackTrace();
			            return 0;
			        }
			        // ����ǰ�������һ��������
			        try {
			            serialPort.addEventListener(this);
			        } catch (TooManyListenersException e) {
			            e.printStackTrace();
			            return 0;
			        }
					// ���ü�������Ч��������������ʱ֪ͨ
					serialPort.notifyOnDataAvailable(true);
			
					// ���ô��ڵ�һЩ��д����
					try {
						// �����ʡ�����λ��ֹͣλ����żУ��λ
							serialPort.setSerialPortParams(115200,
							SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
							SerialPort.PARITY_NONE);
					} catch (UnsupportedCommOperationException e) {
							e.printStackTrace();
							return 0;
					}
			
			        return 1;
			    }
            }
        }
        return 0;
    }
	/*
	* byte����ת��ʮ�������ַ���
	*/
	public String ByteToArray(byte[]data,int len) {
	    String result = "";
	    for(int i = 0; i < len && i < data.length; i++) {
	        result+= Integer.toHexString((data[i] & 0xFF) | 0x100).toUpperCase().substring(1, 3);
	    }
	    return result;
	}
	/*
	* �ϱ���Ϣ���ر���
	*/
	public boolean SaveReportInf(String arg0,String arg1) {
		Calendar currentDate = Calendar.getInstance();
		int today = currentDate.get(Calendar.DATE);
		int month = currentDate.get(Calendar.MONTH) + 1;
		int year = currentDate.get(Calendar.YEAR);

        File file = new File("./workdir/report"+year+"��"+month+"��"+today+"��"+".txt");
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
            // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
		/* ��ȡϵͳʱ�� */
		long time = System.currentTimeMillis();
		Date date = new Date(time);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String stringTime = dateFormat.format(date); 
		/* ���ϱ���Ϣ��ʱ����뱾���ĵ� */
		try {
			FileOutputStream stream = new FileOutputStream(file,true);
			OutputStreamWriter fos = new OutputStreamWriter(stream);
			fos.write(stringTime+" helpID: " + arg0 + " tagID: " + arg1 + "\r\n");
			fos.close();
			return true;
        } catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
        return false;
	}
}
