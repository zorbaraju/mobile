//
//  ViewController.swift
//  second
//
//  Created by Mackbook on 25/07/16.
//  Copyright © 2016 Mackbook. All rights reserved.
//
import CoreBluetooth

import UIKit
import SystemConfiguration
import SystemConfiguration.CaptiveNetwork

class ViewController: UIViewController , CBCentralManagerDelegate, CBPeripheralDelegate  {
    
    var selectedRoomDeviceName:String!;
    var lightsComp:CollapseView!;
    var devicesComp:CollapseView!;
    var groupsComp:CollapseView!;
    var schedulersComp:CollapseView!;
    var bthw:BtHWLayer!
    let dbOperation:DBOperation = DBOperation();
    
    var rooms:[RoomDAO] = [RoomDAO]()
    var roomNames: [String] = [String]()
    let roomMenuItemImages: [UIImage] = []

    @IBOutlet var homeMenu: MenuUIView!
    @IBOutlet var roomMenuView: MenuUIView!
     @IBOutlet var scrollView: UIScrollView!
    @IBOutlet var verticalview: UIStackView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        //view.translatesAutoresizingMaskIntoConstraints = false;
        scrollView.translatesAutoresizingMaskIntoConstraints = false;
        //scrollView.addSubview(view)
        scrollView.addConstraints(NSLayoutConstraint.constraintsWithVisualFormat("V:|[verticalview]|", options: NSLayoutFormatOptions(rawValue:0),metrics: nil, views: ["verticalview":verticalview]))
        scrollView.addConstraints(NSLayoutConstraint.constraintsWithVisualFormat("H:|[verticalview]|", options: NSLayoutFormatOptions(rawValue:0),metrics: nil, views: ["verticalview":verticalview]))
        scrollView.addConstraint(
            NSLayoutConstraint(item: scrollView,
                attribute: .Width,
                relatedBy: .Equal,
                toItem: verticalview,
                attribute: .Width,
                multiplier: 1.0,
                constant: 0))
        
        // Do any additional setup after loading the view, typically from a nib.
        lightsComp = CollapseView(frame: verticalview.bounds, title:"Lights",tag:1);
        lightsComp.registerListeners(self, methodName: "buttonClicked:");
        verticalview.addArrangedSubview(lightsComp)
        devicesComp = CollapseView(frame: verticalview.bounds, title:"Devices",tag:2);
        devicesComp.registerListeners(self, methodName: "buttonClicked:");
        verticalview.addArrangedSubview(devicesComp)
        groupsComp = CollapseView(frame: verticalview.bounds, title:"Groups",tag:3);
        groupsComp.registerListeners(self, methodName: "buttonClicked:");
        verticalview.addArrangedSubview(groupsComp)
        schedulersComp = CollapseView(frame: verticalview.bounds, title:"Schedulers",tag:4);
        schedulersComp.registerListeners(self, methodName: "buttonClicked:");
        verticalview.addArrangedSubview(schedulersComp)
        
        constructRoomList();
        roomMenuView.setParentView1(self, p: view, menuItemImages: roomMenuItemImages, menuItemNames: roomNames);
        
        // home menuItems
        let homeMenuItemImages: [UIImage] = [UIImage(named: "home.png")!,UIImage(named: "home.png")!, UIImage(named: "home.png")!, UIImage(named: "home.png")!]
        let homeMenuItemNames: [String] = ["Add Device", "Help", "About", "Exit"]
        
        homeMenu.setImageButton(UIImage(named: "home.png")!)
        homeMenu.setParentView1(self, p: view, menuItemImages: homeMenuItemImages, menuItemNames: homeMenuItemNames);
        var lastselectedroom:String = dbOperation.getLastSelectedRoom();
        print("Last selected room is \(lastselectedroom)")
        self.roomMenuView.setSelecteditem(lastselectedroom)
        print("going out rajuDHI")
        populateRoomPanel(lastselectedroom)
        printWifi();
        
        bthw = BtHWLayer();
    }
    
        
    func populateRoomPanel(lastSelectedRoom:String) {
        print("populating roompanel for the room\(lastSelectedRoom)")
        selectedRoomDeviceName = lastSelectedRoom
        lightsComp.clearAllDevices();
        devicesComp.clearAllDevices();
        groupsComp.clearAllDevices();
        schedulersComp.clearAllDevices();
        if( lastSelectedRoom != "") {
            let lights:[DeviceDAO] = dbOperation.getLights(lastSelectedRoom);
            print("lightcount......\(lights.count)")
            for var i = 0; i < lights.count; i += 1 {
                let compData:CollapseCompData = CollapseCompData(name: lights[i].deviceName);
                lightsComp.addComp(compData)
            }
            let devices:[DeviceDAO] = dbOperation.getDevices(lastSelectedRoom);
            for var i = 0; i < devices.count; i += 1 {
                let compData:CollapseCompData = CollapseCompData(name: devices[i].deviceName);
                devicesComp.addComp(compData)
            }
            let groups:[GroupDAO] = dbOperation.getGroups(lastSelectedRoom);
            for var i = 0; i < groups.count; i += 1 {
                let compData:CollapseCompData = CollapseCompData(name: groups[i].name);
                groupsComp.addComp(compData)
            }
            let schedulers:[SchedulerDAO] = dbOperation.getSchedulers(lastSelectedRoom);
            for var i = 0; i < schedulers.count; i += 1 {
                let compData:CollapseCompData = CollapseCompData(name: schedulers[i].name);
                schedulersComp.addComp(compData)
            }
        }
        lightsComp.refresh();
        devicesComp.refresh();
        groupsComp.refresh();
        schedulersComp.refresh();
    }
    func constructRoomList() {
        rooms = dbOperation.getRoomList()
        for var i = 0; i < rooms.count; i += 1 {
            roomNames.append(rooms[i].roomName)
        }
    }
    
    func dummy(rName:String) {
        print("going out Dumy\(rName)")
    }
    
    func printWifi() {
        let interfaces = CNCopySupportedInterfaces()
        
        if interfaces != nil {
            print("interface.....")
            let interfacesArray = CFBridgingRetain(interfaces) as! NSArray
             print("interface...num..\(interfacesArray.count)")
            for var i = 0; i < interfacesArray.count; i += 1  {
                let interfaceName = interfacesArray[i] as! String
                print("interface...name..\(interfaceName)")
                let unsafeInterfaceData = CNCopyCurrentNetworkInfo(interfaceName)! as Dictionary
                let SSIDName = unsafeInterfaceData["SSID"] as! String
                print(SSIDName)/* here print recentally used wifi name*/
                print("ssid...\(SSIDName)");
            }
        }
    }
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func buttonClicked(sender: UIButton) {
        print("...\(sender.tag)")
        if( sender.tag >= 1 && sender.tag <= 2) {
            performSegueWithIdentifier("ConfigSegueId", sender: sender)
        }else if( sender.tag == 3 ) {
            performSegueWithIdentifier("GroupSegueId", sender: sender)
        }else if( sender.tag == 4 ) {
            performSegueWithIdentifier("ShedulerSegueId", sender: sender)
        }
            
        else if( sender.tag >= 101 && sender.tag <= 104) {
            print("remove operation of collapse panel \(sender.tag)")
            if( sender.tag == 101) {
                let selindex = lightsComp.getSelectedIndex();
                print("selectindex....\(selindex)   "+selectedRoomDeviceName)
                dbOperation.removeLight(selectedRoomDeviceName, indexAt: selindex);
                lightsComp.removeComp()
            } else if( sender.tag == 102) {
                let selindex = devicesComp.getSelectedIndex();
                print("selectindex....\(selindex)   "+selectedRoomDeviceName)
                dbOperation.removeDevice(selectedRoomDeviceName, indexAt: selindex);
                devicesComp.removeComp()
            } else if( sender.tag == 103) {
                let selindex = groupsComp.getSelectedIndex();
                print("selectindex....\(selindex)   "+selectedRoomDeviceName)
                dbOperation.removeGroup(selectedRoomDeviceName, indexAt: selindex);
                groupsComp.removeComp()
            } else if( sender.tag == 104) {
                let selindex = schedulersComp.getSelectedIndex();
                print("selectindex....\(selindex)   "+selectedRoomDeviceName)
                dbOperation.removeScheduler(selectedRoomDeviceName, indexAt: selindex);
                schedulersComp.removeComp()
            }
        }
        
        
    }
   
    func menuItemClicked(sourceMenu:MenuUIView, rowIndex: Int) {
        print("MenuClciekd...\(roomNames.count)....\(rowIndex)");
        printWifi()
        if( sourceMenu == homeMenu) {
            let sender = UIButton();
            sender.tag = rowIndex
            if( rowIndex  == 1 || rowIndex  == 2 ) {
                performSegueWithIdentifier("HtmlSegueId", sender: sender)
            } else if( rowIndex  == 0 ) {
                performSegueWithIdentifier("DiscoverySegueId", sender: sender)
            }
        } else if( sourceMenu == roomMenuView) {
            var lastroom = roomNames[rowIndex];
            dbOperation.setLastSelectedRoom(lastroom)
            populateRoomPanel(lastroom)
        }
        print("ss434ssss")
        
        
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue!, sender: AnyObject!) {
        print("Segue called from  view controller \(segue.identifier)  \(sender.tag)")
        if (segue.identifier == "HtmlSegueId") {
            var svc = segue!.destinationViewController as! HtmlViewController;
            var title = "Help";
            var file = "help"
            if( sender.tag == 2) {
                file = "about"
                title = "About"
            }
            svc.setHtmlFile(title, htmlfile: file)
        } else if ( segue.identifier == "ConfigSegueId"){
            var svc = segue!.destinationViewController as! ConfigViewController;
            svc.setRoomDeviceName(lightsComp, name: selectedRoomDeviceName, tag: sender.tag)
        } else if ( segue.identifier == "GroupSegueId"){
            var svc = segue!.destinationViewController as! GroupViewController;
            svc.setRoomDeviceName(selectedRoomDeviceName)
        } else if ( segue.identifier == "ShedulerSegueId"){
            var svc = segue!.destinationViewController as! SchedulerViewController;
            svc.setRoomDeviceName(selectedRoomDeviceName)
        }
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

