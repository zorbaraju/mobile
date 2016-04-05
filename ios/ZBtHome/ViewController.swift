//
//  ViewController.swift
//
//  Created by Mackbook on 25/02/16.
//  Copyright © 2016 Mackbook. All rights reserved.
//

import UIKit
import CoreBluetooth

class ViewController: UIViewController, CBCentralManagerDelegate, CBPeripheralDelegate {

    
    var centralManager:CBCentralManager!
    var connectingPeripheral:CBPeripheral!
    var charr:CBCharacteristic!
    var sliderDemo:UISlider!
    
    let picker = UIImageView(image: UIImage(named: "about"))
    
    struct properties {
        static let moods = [
            ["title" : "the best", "color" : "#8647b7"],
            ["title" : "really good", "color": "#4870b7"],
            ["title" : "okay", "color" : "#45a85a"],
            ["title" : "meh", "color" : "#a8a23f"],
            ["title" : "not so great", "color" : "#c6802e"],
            ["title" : "the worst", "color" : "#b05050"]
        ]
    }
    
    @IBAction func pickerSelect(sender: UIButton)
    {
        picker.hidden ? openPicker() : closePicker()
    }
    
    func createPicker()
    {
        picker.frame = CGRect(x: ((self.view.frame.width / 2) - 143), y: 200, width: 286, height: 291)
        picker.alpha = 0
        picker.hidden = true
        picker.userInteractionEnabled = true
        
        var offset = 21
        
        for (index, feeling) in properties.moods.enumerate()
        {
            let button = UIButton()
            button.frame = CGRect(x: 13, y: offset, width: 260, height: 43)
            //button.setTitleColor(UIColor(feeling["color"]), forState: .Normal)
            button.setTitle(feeling["title"], forState: .Normal)
            button.tag = index
            
            picker.addSubview(button)
            
            offset += 44
        }
        
        view.addSubview(picker)
    }
    
    
    func openPicker()
    {
        self.picker.hidden = false
        
        UIView.animateWithDuration(0.3,
            animations: {
                self.picker.frame = CGRect(x: ((self.view.frame.width / 2) - 143), y: 230, width: 286, height: 291)
                self.picker.alpha = 1
        })
    }
    
    func closePicker()
    {
        UIView.animateWithDuration(0.3,
            animations: {
                self.picker.frame = CGRect(x: ((self.view.frame.width / 2) - 143), y: 200, width: 286, height: 291)
                self.picker.alpha = 0
            },
            completion: { finished in
                self.picker.hidden = true
            }
        )
    }
    
    override func viewDidLoad() {
        
        super.viewDidLoad()
        print(UIDevice.currentDevice().userInterfaceIdiom);
        // Do any additional setup after loading the view, typically from a nib.
        print("Raju")
        /*let myFirstLabel = UILabel()
        let myFirstButton = UIButton(frame: CGRectMake(100, 100, 100, 50))
        
        myFirstLabel.text = "I made a label on the screen #toogood4you"
        myFirstLabel.font = UIFont(name: "MarkerFelt-Thin", size: 45)
        myFirstLabel.textColor = UIColor.redColor()
        myFirstLabel.textAlignment = .Center
        myFirstLabel.numberOfLines = 5
        myFirstLabel.frame = CGRectMake(15, 54, 300, 500)
        myFirstButton.setTitle("<<<✸>>>>", forState: .Normal)
        myFirstButton.setTitleColor(UIColor.blueColor(), forState: .Normal)
        myFirstButton.backgroundColor = UIColor.redColor()
        //myFirstButton.frame =

        myFirstButton.addTarget(self, action: "pressed:", forControlEvents: UIControlEvents.TouchUpInside)
        /*myFirstButton.addTarget(self, action: "pressed:", forControlEvents: UIControlEvents.TouchUpInside)*/

        self.view.addSubview(myFirstLabel)
        self.view.addSubview(myFirstButton)
        */
        
        let userDefaults = NSUserDefaults.standardUserDefaults()
        
        
        sliderDemo = UISlider(frame:CGRectMake(20, 260, 280, 20))
        sliderDemo.minimumValue = 0
        sliderDemo.maximumValue = 9
        sliderDemo.continuous = true
        sliderDemo.tintColor = UIColor.redColor()
        sliderDemo.value = userDefaults.floatForKey("slider")
        sliderDemo.addTarget(self, action: "sliderValueDidChange:", forControlEvents: .ValueChanged)
        self.view.addSubview(sliderDemo)
        
        let sv = userDefaults.boolForKey("switch");
        print("Switch value\(sv)");
        let switchDemo=UISwitch(frame:CGRectMake(150, 300, 0, 0));
        switchDemo.on = userDefaults.boolForKey("switch")
        switchDemo.addTarget(self, action: "switchValueDidChange:", forControlEvents: .ValueChanged);
        self.view.addSubview(switchDemo);
        
        let name = userDefaults.objectForKey("name")
        
        if name == nil {
            print(" Name is not stored")
            userDefaults.setObject("Raju", forKey: "name")
            print(" Name is  stored")
        } else {
            print("Name is \(name)")
            
        }
        createPicker()
    }
    
    func switchValueDidChange(sender:UISwitch!)
    {
        let userDefaults = NSUserDefaults.standardUserDefaults()
        userDefaults.setBool(sender.on, forKey: "switch")
        if (sender.on == true){
            print("On")
            var rawArray:[UInt8] = [35,1,1,2,9];
            //rawArray = [41,1];
            //rawArray = [63,1,0xFF];
            let data = NSData(bytes: &rawArray, length: rawArray.count)
            connectingPeripheral.writeValue(data, forCharacteristic: charr, type: CBCharacteristicWriteType.WithoutResponse)
            sliderDemo.hidden = true;
        }
        else{
            print("Off")
            var rawArray:[UInt8] = [35,1,1,2,0];
            //rawArray = [41,1];
            //rawArray = [63,1,0xFF];
            let data = NSData(bytes: &rawArray, length: rawArray.count)
            connectingPeripheral.writeValue(data, forCharacteristic: charr, type: CBCharacteristicWriteType.WithoutResponse)
            sliderDemo.hidden = false;
        }
    }
    
    func pressed(sender:UIButton!){
        print("sddssdsd")
        print(sender.currentTitle)
        print("Pressed \(sender.currentTitle)")
    }
    func sliderValueDidChange(sender:UISlider!)
    {
        let userDefaults = NSUserDefaults.standardUserDefaults()
        userDefaults.setFloat(sender.value, forKey: "slider")
        print("value--\(Int(sender.value))")
        let val = UInt8(bitPattern: Int8(sender.value));
        var rawArray:[UInt8] = [35,1,1,2,val];
        //rawArray = [41,1];
        //rawArray = [63,1,0xFF];
        let data = NSData(bytes: &rawArray, length: rawArray.count)
        connectingPeripheral.writeValue(data, forCharacteristic: charr, type: CBCharacteristicWriteType.WithoutResponse)
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    required init(coder aDecoder: NSCoder) {
        
        super.init(coder: aDecoder)!
        centralManager = CBCentralManager(delegate: self, queue: dispatch_get_main_queue())
    }
    
    func centralManagerDidUpdateState(central: CBCentralManager){
        
        switch central.state{
        case .PoweredOn:
            print("poweredOn")
            
            let serviceUUIDs:[CBUUID] = []//[CBUUID(string: "0000ffe0-0000-1000-8000-00805f9b34fb")]
            let lastPeripherals = centralManager.retrieveConnectedPeripheralsWithServices(serviceUUIDs)
            
            if lastPeripherals.count > 0{
                let device = lastPeripherals.last! as CBPeripheral;
                connectingPeripheral = device;
                centralManager.connectPeripheral(connectingPeripheral, options: nil)
                print("connected")
            }
            else {
                centralManager.scanForPeripheralsWithServices(serviceUUIDs, options: nil)
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
        centralManager.connectPeripheral(connectingPeripheral, options: nil)
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

