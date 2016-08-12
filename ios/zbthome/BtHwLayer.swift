import UIKit
import CoreBluetooth

class BtHWLayer: NSObject, CBCentralManagerDelegate, CBPeripheralDelegate {
    
    var centralManager:CBCentralManager!
    
    override init() {
        super.init()
        print("bthwlayer>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")
        centralManager = CBCentralManager(delegate: self, queue: dispatch_get_main_queue())
  }
   
    var connectingPeripheral:CBPeripheral!
    var charr:CBCharacteristic!
    
    func centralManagerDidUpdateState(central: CBCentralManager){
        print("centralManagerDidUpdateState.")
        switch central.state{
        case .PoweredOn:
            print("poweredOn")
            
            let serviceUUIDs:[CBUUID] = []//[CBUUID(string: "0000ffe0-0000-1000-8000-00805f9b34fb")]
            let lastPeripherals = central.retrieveConnectedPeripheralsWithServices(serviceUUIDs)
            
            if lastPeripherals.count > 0{
                let device = lastPeripherals.last! as CBPeripheral;
                connectingPeripheral = device;
                central.connectPeripheral(connectingPeripheral, options: nil)
                print("connected")
            }
            else {
                central.scanForPeripheralsWithServices(serviceUUIDs, options: nil)
                print("Scan")
            }
            
        default:
            print(central.state)
        }
    }
    
    func centralManager(central: CBCentralManager, didDiscoverPeripheral peripheral: CBPeripheral, advertisementData: [String : AnyObject], RSSI: NSNumber) {
        print("centralManager.... discover peripheral....")
        connectingPeripheral = peripheral
        connectingPeripheral.delegate = self
        central.connectPeripheral(connectingPeripheral, options: nil)
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
                        var rawArray:[UInt8] = [35,1,1,1,0];
                        //rawArray = [41,1];
                        rawArray = [63,1,0xFF];
                        let data = NSData(bytes: &rawArray, length: rawArray.count)
                        peripheral.writeValue(data, forCharacteristic: characteristic, type: CBCharacteristicWriteType.WithoutResponse)
                        
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
                print(characteristic.value?.length)
                print(characteristic.value)
            default:
                print("Default")
            }
        }
    }
}
