//
//  AboutViewController.swift
//  second
//
//  Created by Mackbook on 28/07/16.
//  Copyright © 2016 Mackbook. All rights reserved.
//

import CoreBluetooth

import UIKit

class DiscoveryViewController: UIViewController ,UICollectionViewDelegateFlowLayout, UICollectionViewDataSource , CBCentralManagerDelegate {

    var selectedConfiguredRoomIndex = -1;
    var configuredRooms:[RoomDAO] = [RoomDAO]()
    @IBOutlet var deleteButton: UIButton!
    
    var discoveredDeviceIds:[String] = [String]()
    var centralManager:CBCentralManager!
    @IBOutlet var configuredView: UICollectionView!
    
    @IBOutlet var discoveredDevicesPanel: UIStackView!
    var discoveredDevices:[LabelTextView] = [LabelTextView]()
    
    @IBOutlet var scrollView: UIScrollView!
    @IBOutlet var verticalview: UIStackView!
    let phoneMemory = NSUserDefaults.standardUserDefaults()
    override func viewDidLoad() {
        super.viewDidLoad()

        deleteButton.hidden = true;
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
        
        configuredView.dataSource = self
        configuredView.delegate = self
        configuredView.registerClass(ZIconCell.self, forCellWithReuseIdentifier: "Cell")
        configuredView.backgroundColor = UIColor.whiteColor()
        
        loadConfiguredRooms()
        let tapper = UITapGestureRecognizer(target: view, action:#selector(UIView.endEditing))
        tapper.cancelsTouchesInView = false
        view.addGestureRecognizer(tapper)

    }
    
    @IBAction func cancelButton(sender: UIButton) {
        
    }
   
    func loadConfiguredRooms() {
        configuredRooms = DBOperation().getRoomList();
    }
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func centralManagerDidUpdateState(central: CBCentralManager!) {
        print("checking state")
        
        if central.state == .PoweredOn {
            // In a real app, you'd deal with all the states correctly
            print("Got on")
            let serviceUUIDs:[CBUUID] = []//[CBUUID(string: "0000ffe0-0000-1000-8000-00805f9b34fb")]
            let lastPeripherals:[CBPeripheral] = centralManager.retrieveConnectedPeripheralsWithServices(serviceUUIDs)
            
            if lastPeripherals.count > 0{
                let device = lastPeripherals.last! as CBPeripheral;
                //connectingPeripheral = device;
                centralManager.connectPeripheral(device, options: nil)
                print("connected")
            }
            else {
                centralManager.scanForPeripheralsWithServices(serviceUUIDs, options: nil)
                print("Scan")
            }
        } else {
            print("Got off")
        }
    }
    
    func centralManager(central: CBCentralManager, didDiscoverPeripheral peripheral: CBPeripheral, advertisementData: [String : AnyObject], RSSI: NSNumber) {
        print("centralManager.... discover peripheral....\(peripheral.name) id:\(peripheral.identifier.UUIDString)")
        if( advertisementData.indexForKey("kCBAdvDataLocalName") == nil) {
            return
        }
        if( DBOperation().isRoomExist(peripheral.identifier.UUIDString)) {
            print("centralManager.... discover peripheral....\(peripheral.name) id:\(peripheral.identifier.UUIDString) is already configured")
            return;
        }
        discoveredDeviceIds.append(peripheral.identifier.UUIDString);
        let name:String = peripheral.name!
       
        let deviceComp = LabelTextView(frame: discoveredDevicesPanel.bounds, title:name);
        discoveredDevicesPanel.addArrangedSubview(deviceComp)
        discoveredDevices.append(deviceComp)
    }
    
    
    
    func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        print("Collection view numcomps...\(1)")
        return configuredRooms.count;
    }
    
    func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCellWithReuseIdentifier("Cell", forIndexPath: indexPath) as! ZIconCell
        let room = configuredRooms[indexPath.row];
        cell.backgroundColor = UIColor.orangeColor()
        cell.setLabelAndImage(room.roomName,  imageName: "about.png")
        return cell
    }
    
    func collectionView(collectionView: UICollectionView, didSelectItemAtIndexPath indexPath: NSIndexPath) {
        deleteButton.hidden = false;
        selectedConfiguredRoomIndex = indexPath.row;
    }
    
    @IBAction func startDiscovery(sender: UIButton) {
        centralManager = CBCentralManager(delegate: self, queue: dispatch_get_main_queue())
        print("CEntral manager is created")
        discoveredDeviceIds = [];
    }
    @IBAction func deleteRoom(sender: UIButton) {
        DBOperation().removeRoom(selectedConfiguredRoomIndex);
        configuredRooms.removeAtIndex(selectedConfiguredRoomIndex)
        configuredView.reloadData()
        deleteButton.hidden = true
    }
    
    @IBAction func addRoomAction(sender: UIButton) {
        if (discoveredDevices.count>0)  {
            var isNameFilled = false;
            for var i = 0; i < discoveredDevices.count; i += 1 {
                let lt:LabelTextView = discoveredDevices[i];
                let inputName = lt.getValue();
                if(!inputName.isEmpty) {
                    isNameFilled = true;
                    break;
                }
            }
            if( !isNameFilled ) {
                let alert = UIAlertView();
                alert.title = "Title"
                alert.message = "Room name is empty"
                alert.addButtonWithTitle("Ok")
                alert.show()
                return;
            }
            for var i = 0; i < discoveredDevices.count; i += 1 {
                let lt:LabelTextView = discoveredDevices[i];
                let inputName = lt.getValue();
                if(!inputName.isEmpty) {
                    let newroom:RoomDAO = RoomDAO(deviceName: discoveredDeviceIds[i], roomName: inputName)
                    DBOperation().addRoom(newroom);
                    performSegueWithIdentifier("goBackFromRoomAdd", sender: nil)
                }
            }
        }
       
    }

}
