package main;

import java.util.Random;

import com.huawei.iota.iodev.IodevService;
import com.huawei.iota.iodev.IotaDeviceInfo;
import com.huawei.iota.iodev.hub.HubService;
import com.huawei.iota.util.IotaMessage;
import com.huawei.iota.util.MyObserver;

import bean.GatewayInfo;

public class AgentLiteHub implements MyObserver {
	public static IotaDeviceInfo deviceInfo = null;
	
	private static AgentLiteHub instance = new AgentLiteHub();
	
    public static AgentLiteHub getInstance() {
        return instance;
    }
	
	public void addSensor() {
        System.out.println(" ============= addSensor! ============== ");
        int cookie;
        Random random = new Random();
        cookie = random.nextInt(65535);

        String nodeId = "5432154321";
        String manufatrueId = "Huawei";
        String deviceType = "Motion";
        String model = "test01";
        String protocolType = "MQTT";
        deviceInfo = new IotaDeviceInfo(nodeId, manufatrueId, deviceType, model, protocolType);
        HubService.addDevice(cookie, deviceInfo);
	}
	
	public void updataDeviceStatus() {
		System.out.println(" =============  updataDeviceStatus  ============== ");
        int cookie;
        Random random = new Random();
        cookie = random.nextInt(65535);
        String sensorId = GatewayInfo.getSensorId();
        System.out.println("cookie = " + cookie);
        System.out.println("sensorId = " + sensorId);

        HubService.updateDeviceStatus(cookie, sensorId, "ONLINE", "NONE");
	}
	
    public void rmvSensor() {
        System.out.println(" =============  rmvSensor!  ============== ");
        int cookie;
        Random random = new Random();
        cookie = random.nextInt(65535);
        String sensorId = GatewayInfo.getSensorId();
        System.out.println("rmvSensor deviceId = " + sensorId);
        HubService.rmvDevice(cookie, sensorId);
    }
    
    private void getAddDeviceAnswer(IotaMessage iotaMsg) {
    	String deviceId = iotaMsg.getString(HubService.HUB_IE_DEVICEID);;
    	int ret = iotaMsg.getUint(HubService.HUB_IE_RESULT, HubService.HUB_RESULT_FAILED);
        if (ret == HubService.HUB_RESULT_SUCCESS) {
        	System.out.println("Add device :" + deviceId + " success");
        	GatewayInfo.setSensorId(deviceId);
        }
        if (ret == HubService.HUB_RESULT_DEVICE_EXIST) {
        	System.out.println(" =============   Add device[" + deviceId + "] has exit! ============== ");
        }
    }
    
    private void getRmvDeviceAnswer(IotaMessage iotaMsg) {
        int result = iotaMsg.getUint(HubService.HUB_IE_RESULT, 0);
        int cookie = iotaMsg.getUint(HubService.HUB_IE_COOKIE, 0);
        System.out.println("rmv device result = " + result);
        System.out.println("cookie = " + cookie);
        if (0 == result) {
        	System.out.println("rmv device success!");
        } else {
        	System.out.println("rmv device fail!");
        }
    }
    
    private void getUpdateStatusAnswer(IotaMessage iotaMsg) {
        int result = iotaMsg.getUint(HubService.HUB_IE_RESULT, 0);
        int cookie = iotaMsg.getUint(HubService.HUB_IE_COOKIE, 0);
        String deviceId = iotaMsg.getString(HubService.HUB_IE_DEVICEID);
        System.out.println("cookie = " + cookie);
        if (result == 0) {
            System.out.println( "update device[" + deviceId + "] status success!");
            //rmvSensor();
        } else if (result == HubService.HUB_RESULT_DEVICE_NOTEXIST) {
            System.out.println("device[" + deviceId + "] is not exit!");
        } else {
            System.out.println("update device[" + deviceId + "] status fail!");
        }
    }
    
    private void getUnbindAnswer(IotaMessage iotaMsg) {
    	System.out.println("recevie unbind CMD!!!");
    }

	@Override
	public void update(IotaMessage arg0) {
		System.out.println("�յ�hubservice֪ͨ:" + arg0);
		int mMsgType = arg0.getMsgType();
		switch(mMsgType) {
		//�յ�����豸��Ӧ��
		case IodevService.IODEV_MSG_ADD_DEVICE_RSP:
			getAddDeviceAnswer(arg0);
			break;
		//�յ�ɾ���豸��Ӧ��
		case IodevService.IODEV_MSG_RMV_DEVICE_RSP:
			getRmvDeviceAnswer(arg0);
			break;
		//�յ������豸��Ϣ��Ӧ��
		case IodevService.IODEV_MSG_UPDATE_DEVSTATUS_RSP:
			getUpdateStatusAnswer(arg0);
			break;
		case IodevService.IODEV_MSG_RECEIVE_CMD:
			getUnbindAnswer(arg0);
			break;
			
		default:
			break;
	}
	}    
}
