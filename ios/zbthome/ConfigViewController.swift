//
//  ConfigViewController.swift
//  second
//
//  Created by Mackbook on 26/07/16.
//  Copyright © 2016 Mackbook. All rights reserved.
//

import UIKit

class ConfigViewController: MenuViewController {

    @IBOutlet var deviceIdMenu: MenuUIView!
    @IBOutlet var deviceTypeMenu: MenuUIView!
    var collapseComp:CollapseView!
    var roomDeviceName:String!
    var daoType:Int!

    var deviceIdMenuNames:[String] = [
        "1",
        "2"
    ]
    
    var deviceTypeMenuNames:[String] = [
        "Light",
        "Fan"
    ]
    
    @IBOutlet var nameText: UITextField!
    @IBOutlet var deviceIdText: UITextField!
    
    override func viewDidLoad() {
        super.viewDidLoad()

        deviceIdMenu.setParentView1(self, p: view, menuItemImages: [], menuItemNames: deviceIdMenuNames);
        deviceTypeMenu.setParentView1(self, p: view, menuItemImages: [], menuItemNames: deviceTypeMenuNames);
        
        // Do any additional setup after loading the view.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func setRoomDeviceName(collapseComp:CollapseView, name:String, tag:Int) {
        self.collapseComp = collapseComp
        roomDeviceName = name
        daoType = tag
    }
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        print("prepareForSegue.....\(segue.identifier)"+roomDeviceName+"  daotype=\(daoType)")
        if( segue.identifier == "goBackFromSave") {
            var name = nameText.text
            if( daoType == 1) {
                
                if( (name == "") ) {
                    let alert = UIAlertView();
                    alert.title = "Title"
                    alert.message = "Device name is empty"
                    alert.addButtonWithTitle("Ok")
                    alert.show()
                } else {
                    let light:DeviceDAO = DeviceDAO(deviceName: name!, deviceId: Int(deviceIdText.text!)!);
                    DBOperation.getInstance().addLight(roomDeviceName, light: light)
                }
            } else if( daoType == 2) {
                let light:DeviceDAO = DeviceDAO(deviceName: nameText.text!, deviceId: Int(deviceIdText.text!)!);
                DBOperation.getInstance().addDevice(roomDeviceName, light: light)
           }
            
        }
        
    }
    
    override func menuItemClicked(sourceMenu:MenuUIView, rowIndex: Int) {
    }
}
