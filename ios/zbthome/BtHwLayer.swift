import UIKit
import CoreBluetooth

class BtHWLayer: NSObject, CBCentralManagerDelegate, CBPeripheralDelegate {
    
    var dataQueue:[UInt8:[UInt8]] = [UInt8:[UInt8]]()
    
    let group = dispatch_group_create()
    let queue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 2)
    
    
    var connectingPeripheral:CBPeripheral!
    var charr:CBCharacteristic!
    
    var deviceUUIDString:String!
    var centralManager:CBCentralManager!
    var connected:Bool = false
    
    class func getInstance()->BtHWLayer {
        struct Static {
            static let instance = BtHWLayer()
        }
        return Static.instance
    }

    private override init() {
        super.init()
        centralManager = CBCentralManager(delegate: self, queue: queue)
    }
    
    func initDevice(deviceUUIDString:String) {
        if( self.deviceUUIDString != deviceUUIDString) {
            closeDevice()
        }
        self.deviceUUIDString = deviceUUIDString;
        print("11Scan started and wating for 5 sec")
        dispatch_group_enter(group);
        self.centralManager.scanForPeripheralsWithServices([], options: nil)
        print("22Scan started and wating for 5 sec")
        let maxWait = dispatch_time(DISPATCH_TIME_NOW, Int64(1 * NSEC_PER_SEC))
        dispatch_group_wait(group, maxWait);
        print("33Scan started and wating for 5 sec")
        
    }
    
    func isConnected()->Bool {
        
        return connected
    }
   
    func closeDevice() {
        charr = nil;
        if( connectingPeripheral != nil) {
            centralManager.cancelPeripheralConnection(connectingPeripheral);
        }
        connectingPeripheral = nil;
        connected = false;
        print("Closed the connection")
    }

    func centralManagerDidUpdateState(central: CBCentralManager){
        print("centralManagerDidUpdateState.")
        switch central.state{
        case .PoweredOn:
            print("poweredOn")
            //central.scanForPeripheralsWithServices([], options: nil)
        case .PoweredOff:
            print("PoweredOff")
            closeDevice()
        default:
            print(central.state)
        }
    }
    
    func centralManager(central: CBCentralManager, didDiscoverPeripheral peripheral: CBPeripheral, advertisementData: [String : AnyObject], RSSI: NSNumber) {
        print("centralManager.... discover peripheral...."+peripheral.identifier.UUIDString);
        if( connected == false && peripheral.identifier.UUIDString == deviceUUIDString) {
            connectingPeripheral = peripheral
            connectingPeripheral.delegate = self
            central.stopScan()
            central.connectPeripheral(connectingPeripheral, options: nil)
            print("Connected to given device")
            connected = true
        }
    }
    
    
    func centralManager(central: CBCentralManager, didConnectPeripheral peripheral: CBPeripheral) {
        print("centralManager.... discover serivces....")
        
        peripheral.discoverServices(nil)
    }
    
    func peripheral(peripheral: CBPeripheral, didDiscoverServices error: NSError?) {
        print("peripheral....");
        if let _ = error{
            
        }
        else {
            for service in peripheral.services as [CBService]!{
                peripheral.discoverCharacteristics(nil, forService: service)
                print("peripheral....discover charr");
            }
        }
    }
    
    func peripheral(peripheral: CBPeripheral, didDiscoverCharacteristicsForService service: CBService, error: NSError?) {
        print("periperal...2")
        if let _ = error{
            
        }
        else {
            print("serivce uuid....")
            print(service.UUID)
            if service.UUID == CBUUID(string: "0000ffe0-0000-1000-8000-00805f9b34fb"){
                print("serivce uuid..got..")
                for characteristic in service.characteristics! as [CBCharacteristic]{
                    switch characteristic.UUID.UUIDString{
                        
                    case "2A37":
                        // Set notification on heart rate measurement
                        print("Found a Heart Rate Measurement Characteristic")
                        peripheral.setNotifyValue(true, forCharacteristic: characteristic)
                        
                    case "2A38":
                        // Read body sensor location
                        print("Found a Body Sensor Location Characteristic")
                        peripheral.readValueForCharacteristic(characteristic)
                        
                    case "FFE1":
                        // Write heart rate control point
                        print("Found a Heart Rate Control Point Characteristic")
                        peripheral.setNotifyValue(true, forCharacteristic: characteristic)
                        charr = characteristic;
                        print("leaving group")
                        dispatch_group_leave(group)
                        print("leaving nti")
                        dispatch_group_notify(group, queue, {})
                        print("notified")
                    default:
                        print("Default ")
                        print(characteristic.UUID.UUIDString)
                    }
                    
                }
            } else {
                print("serivce uuid..not...matchedgot..")
            }
        }
    }
    
    
    func peripheral(peripheral: CBPeripheral, didUpdateValueForCharacteristic characteristic: CBCharacteristic, error: NSError?) {
        print("charuuid...")
        
        print(characteristic.UUID.UUIDString)
        if let _ = error{
            
        }else {
            switch characteristic.UUID.UUIDString{
            case "FFE1":
               // print(characteristic.value?.length)
               // print(characteristic.value)
                //var data = characteristic.value
               // var values = [UInt8](count:data!.length, repeatedValue:0)
               // data!.getBytes(&values, length:data!.length)
               // print("Data: \(data)")
                let data = characteristic.value
                var values = [UInt8](count:data!.length, repeatedValue:0)
                print("values....\(values)")
                print("Data: \(data)")
               data!.getBytes(&values, length:data!.length)
                print("Data: \(data)")
                print("Datavalues : \(values[0]) \(values[1])...\(dataQueue)")
                dataQueue[values[1]] = values;
                dispatch_group_leave(group)
                dispatch_group_notify(group, queue, {})
            default:
                print("Default")
            }
        }
    }
    
    func verifyPwd() {
         let reqid = getReqId();
        let pwd = "EZORBA1234";
        var buf = [UInt8](pwd.utf8)
        var rawArray:[UInt8] = [0x41,reqid];
        for i in 0 ..< buf.count {
            rawArray.append(buf[i]);
        }
        
        rawArray.append(0)
        rawArray.append(0)
        // rawArray = [35,1,1,1,0];
        //rawArray = [41,5];
        //rawArray = [63,1,0xFF];
        let data = NSData(bytes: &rawArray, length: rawArray.count)
        connectingPeripheral.writeValue(data, forCharacteristic: charr, type: CBCharacteristicWriteType.WithoutResponse)
        print("sending request for getting the number of devices")
        getData(reqid);

    }
    
    var reqinc:UInt8 = 1;
    
    func getReqId()->UInt8 {
        reqinc += 1;
        return reqinc;
    }
    
    func getData(reqid:UInt8)->[UInt8] {
        dispatch_group_enter(group);
        var resposeData = dataQueue[reqid];
        if( resposeData == nil) {
            let maxWait = dispatch_time(DISPATCH_TIME_NOW, Int64(5 * NSEC_PER_SEC))
            dispatch_group_wait(group, maxWait);
            resposeData = dataQueue[reqid];
            print("Data.....\(reqid) is \(resposeData)")
        }
        return resposeData!;
    }
    func getNumberOfDevices()->Int {
        let reqid = getReqId();
        var rawArray:[UInt8] = [41,reqid];
       let data = NSData(bytes: &rawArray, length: rawArray.count)
        connectingPeripheral.writeValue(data, forCharacteristic: charr, type: CBCharacteristicWriteType.WithoutResponse)
        print("sending request for getting the number of devices")
        var responseData:[UInt8] = getData(reqid);
        return Int(responseData[2]);
    }
    func getAllStatus()->[UInt8] {
         let reqid = getReqId();
        var rawArray:[UInt8] = [41,reqid];
        
        rawArray = [35,reqid,1,1,0];
        //rawArray = [41,5];
        rawArray = [63,reqid,0xFF];
        let data = NSData(bytes: &rawArray, length: rawArray.count)
        connectingPeripheral.writeValue(data, forCharacteristic: charr, type: CBCharacteristicWriteType.WithoutResponse)
        print("sending request for getting all staus")
        let responseData:[UInt8] = getData(reqid);
        return responseData;
    }
    
    func setDeviceStatus(devId:Int, value:Int) {
        let reqid = getReqId();
        var rawArray:[UInt8] = [35,reqid,1, UInt8(devId), UInt8(value)];
        let data = NSData(bytes: &rawArray, length: rawArray.count)
        connectingPeripheral.writeValue(data, forCharacteristic: charr, type: CBCharacteristicWriteType.WithoutResponse)
        print("sending request for getting the number of devices")
        var responseData:[UInt8] = getData(reqid);
    }
}
