//
//  ConfigViewController.swift
//  second
//
//  Created by Mackbook on 26/07/16.
//  Copyright © 2016 Mackbook. All rights reserved.
//

import UIKit

class ConfigViewController: MenuViewController {

    let dbOperation: DBOperation = DBOperation.getInstance();
    @IBOutlet var deviceIdMenu: MenuUIView!
    @IBOutlet var titleLabel: UILabel!
    @IBOutlet var deviceTypeIconMenu: IconMenu!
    @IBOutlet var dimmableBox: CheckBoxView!
    var collapseComp:CollapseView!
    var roomDeviceName:String!
    var daoType:Int!

    var deviceIdMenuNames:[[String]] = [[String]]()
    
    var lightTypeMenuNames:[[String]] = [
        ["Light","light_off.png"],
        ["Tube Light","fan_off.png"]
    ]
    
    var dimmableLightTypeMenuNames:[[String]] = [
        ["Led","led_off.png"]
    ]
    
    var deviceTypeMenuNames:[[String]] = [
        ["Motor","light_off.png"],
        ["Computer","fan_off.png"]
    ]
    
    var dimmableDeviceTypeMenuNames:[[String]] = [
        ["Ac","light_off.png"],
        ["Fan","fan_off.png"]
    ]
    
    @IBOutlet var nameText: UITextField!
    @IBOutlet var deviceIdText: UITextField!
    
    var currentDeviceDAO:DeviceDAO!
    var indexAt: Int = -1
    
    override func viewDidLoad() {
        super.viewDidLoad()

        deviceIdMenu.setParentView1(self, p: view,menuItemNames: deviceIdMenuNames);
        deviceTypeIconMenu.setParentView1(self, p: view, menuItemNames: lightTypeMenuNames);
        dimmableBox.performButtonClicked(self, callback: "dimmableClicked:")
        // Do any additional setup after loading the view.
        
        print("viewDidLoad")
        if( currentDeviceDAO != nil) {
            if( daoType == 201) {
                titleLabel.text = "Editing Light " + currentDeviceDAO.deviceName
            } else if( daoType == 202) {
                titleLabel.text = "Editing Device " + currentDeviceDAO.deviceName
            }
            nameText.text = currentDeviceDAO.deviceName
            dimmableBox.isChecked = currentDeviceDAO.isdimmable
            dimmableClicked(dimmableBox)
            deviceIdMenu.setSelecteditem(String(currentDeviceDAO.deviceId))
            deviceTypeIconMenu.setSelecteditem(currentDeviceDAO.deviceType)
        } else  {
            if( daoType == 1) {
                titleLabel.text = "Adding a Light"
            } else if( daoType == 2) {
                titleLabel.text = "Adding a Device"
            }
            dimmableClicked(dimmableBox)
            deviceIdMenu.clearSelection()
            deviceTypeIconMenu.clearSelection()
        }
    }

    func dimmableClicked(sender: CheckBoxView) {
        print("dimmable clicked")
        let isDimmable = sender.isChecked;
         print("dimmable clicked isdimmable...\(isDimmable) \(daoType)" )
        if( daoType == 1 || daoType == 201) { // this is for light
            if( isDimmable) {
                deviceTypeIconMenu.setMenuItems(dimmableLightTypeMenuNames);
            } else {
                 deviceTypeIconMenu.setMenuItems(lightTypeMenuNames);
            }
        } else {
            if( isDimmable) {
                deviceTypeIconMenu.setMenuItems(dimmableDeviceTypeMenuNames);
            } else {
                deviceTypeIconMenu.setMenuItems(deviceTypeMenuNames);
            }
        }
        
    }
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func setRoomDeviceName(collapseComp:CollapseView, name:String, tag:Int) {
        self.collapseComp = collapseComp
        roomDeviceName = name
        daoType = tag
        let roomDao:RoomDAO = dbOperation.getLastSelectedRoom();
        var dictionary = Dictionary<Int, Int>()
        let numDevices = roomDao.numDevices
        for (var i: Int = 1; i <= numDevices; i += 1) {
            dictionary[i] = i
        }
        let lights:[DeviceDAO] = dbOperation.getLights(roomDeviceName);
        for (var i: Int = 0; i < lights.count; i += 1) {
            dictionary.removeValueForKey(lights[i].deviceId)
        }
        let devices:[DeviceDAO] = dbOperation.getDevices(roomDeviceName);
        for (var i: Int = 0; i < devices.count; i += 1) {
            dictionary.removeValueForKey(devices[i].deviceId)
        }

        deviceIdMenuNames = [[String]]()
        for (var key: Int = 1; key <= numDevices; key += 1) {
            let keyExists = dictionary[key] != nil
            if( keyExists ) {
                deviceIdMenuNames.append([String(key),""])
            }
        }
    }
    override func shouldPerformSegueWithIdentifier(identifier: String, sender: AnyObject?) -> Bool {
        if( identifier != "goBackFromSave") {
            return true
        }
        let name = nameText.text
        print("Name from configname...\(name)")
        if( (name == "") ) {
            let alert = UIAlertView();
            alert.title = "Error"
            alert.message = "Device name is empty"
            alert.addButtonWithTitle("Ok")
            alert.show()
            return false
        }
        let deviceId = deviceIdMenu.getSelectedText()
        print("deviceId from configname...\(deviceId)")
        if( (deviceId == "") ) {
            let alert = UIAlertView();
            alert.title = "Error"
            alert.message = "Device id is empty"
            alert.addButtonWithTitle("Ok")
            alert.show()
            return false
        }
        let deviceType = deviceTypeIconMenu.getSelectedText()
        print("deviceType from configname...\(deviceType)")
        if( (deviceType == "") ) {
            let alert = UIAlertView();
            alert.title = "Error"
            alert.message = "Type is not selected"
            alert.addButtonWithTitle("Ok")
            alert.show()
            return false
        }
        return true
    }
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        print("prepareForSegue.....\(segue.identifier)"+roomDeviceName+"  daotype=\(daoType)")
        if( segue.identifier == "goBackFromSave") {
            let name = nameText.text
            let deviceId = deviceIdMenu.getSelectedText()
            let isdimmable = dimmableBox.isChecked
            let deviceType = deviceTypeIconMenu.getSelectedText()
            print("device type.......(devicetype.....)\(deviceType)")
            if( daoType == 1 || daoType == 201) {
                let light:DeviceDAO = DeviceDAO(deviceName: name!, deviceId: Int(deviceId)!, isdimmable: isdimmable, deviceType: deviceType);
                if( daoType == 1 ) {
                    dbOperation.addLight(roomDeviceName, light: light)
                } else {
                    dbOperation.updateLight(roomDeviceName, light: light, indexAt:indexAt)
                }
                
            } else if( daoType == 2 || daoType == 202) {
                let light:DeviceDAO = DeviceDAO(deviceName: name!, deviceId: Int(deviceId)!,isdimmable: isdimmable, deviceType: deviceType);
                if( daoType == 2 ) {
                    dbOperation.addDevice(roomDeviceName, light: light)
                } else {
                    dbOperation.updateDevice(roomDeviceName, light: light, indexAt:indexAt)
                }
           }
            
        }
        
    }
    
    override func menuItemClicked(sourceMenu:UIView, rowIndex: Int) {
    }
    
    func updateDAO(device: DeviceDAO, indexAt: Int) {
        print("Update..........DAO  ....\(device.deviceName)")
        currentDeviceDAO = device
        self.indexAt = indexAt
        deviceIdMenuNames.append([String(currentDeviceDAO.deviceId),""])
    }
}
