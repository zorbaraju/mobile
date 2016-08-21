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

class ViewController: MenuViewController, UIGestureRecognizerDelegate  {
    
    var selectedRoomDeviceName:String!;
    var lightsComp:CollapseView!;
    var devicesComp:CollapseView!;
    var groupsComp:CollapseView!;
    var schedulersComp:CollapseView!;
    var bthw:BtHWLayer!
    let dbOperation:DBOperation = DBOperation.getInstance();
    let queue  = dispatch_queue_create("Test", DISPATCH_QUEUE_CONCURRENT)
    
    var rooms:[RoomDAO] = [RoomDAO]()
    var roomNames: [[String]] = []

    @IBOutlet var homeMenu: MenuUIView!
    @IBOutlet var roomMenuView: MenuUIView!
     @IBOutlet var scrollView: UIScrollView!
    @IBOutlet var verticalview: UIStackView!
    
   /* override func viewDidLoad() {
        super.viewDidLoad()
    
    }*/
     override func viewDidLoad() {
        super.viewDidLoad()
        
        bthw = BtHWLayer.getInstance();
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
        roomMenuView.setParentView1(self, p: view, menuItemNames: roomNames);
        
        // home menuItems
        let homeMenuItemNames: [[String]] = [
            ["Add Device", "discovery.png"],
            ["Help","help.png"],
            ["About","about.png"],
            ["Exit", "exit.png"]
            ]
        
        homeMenu.setImageButton(UIImage(named: "home.png")!)
        homeMenu.setParentView1(self, p: view, menuItemNames: homeMenuItemNames);
        let lastselectedroom:RoomDAO = dbOperation.getLastSelectedRoom();
        print("Last selected room is \(lastselectedroom.roomName)")
        self.roomMenuView.setSelecteditem(lastselectedroom.roomName)
        print("going out rajuDHI")
        populateRoomPanel(lastselectedroom)
        printWifi();
        let tap = UITapGestureRecognizer(target: self, action: #selector(ViewController.handleTap))
        tap.delegate = self
        //view.addGestureRecognizer(tap)

    }
    func handleTap() {
        print("tap working")
    }

    func gestureRecognizer(gestureRecognizer: UIGestureRecognizer, shouldReceiveTouch touch: UITouch) -> Bool {
        return !CGRectContainsPoint(homeMenu.bounds, touch.locationInView(homeMenu))

    }
    
    func populateRoomPanel(lastSelectedRoom:RoomDAO) {
        print("populating roompanel for the room\(lastSelectedRoom.roomName)")
        selectedRoomDeviceName = lastSelectedRoom.roomName
        lightsComp.clearAllDevices();
        devicesComp.clearAllDevices();
        groupsComp.clearAllDevices();
        schedulersComp.clearAllDevices();
        if( lastSelectedRoom != "") {
            let lights:[DeviceDAO] = dbOperation.getLights(lastSelectedRoom.roomName);
            print("lightcount......\(lights.count)")
            for i in 0 ..< lights.count {
                let compData:CollapseCompData = CollapseCompData(name: lights[i].deviceName);
                lightsComp.addComp(compData)
            }
            let devices:[DeviceDAO] = dbOperation.getDevices(lastSelectedRoom.roomName);
            for i in 0 ..< devices.count {
                let compData:CollapseCompData = CollapseCompData(name: devices[i].deviceName);
                devicesComp.addComp(compData)
            }
            let groups:[GroupDAO] = dbOperation.getGroups(lastSelectedRoom.roomName);
            for i in 0 ..< groups.count {
                let compData:CollapseCompData = CollapseCompData(name: groups[i].name);
                groupsComp.addComp(compData)
            }
            let schedulers:[SchedulerDAO] = dbOperation.getSchedulers(lastSelectedRoom.roomName);
            for i in 0 ..< schedulers.count {
                let compData:CollapseCompData = CollapseCompData(name: schedulers[i].name);
                schedulersComp.addComp(compData)
            }
        }
        lightsComp.refresh();
        devicesComp.refresh();
        groupsComp.refresh();
        schedulersComp.refresh();
        print("populating roompanel for the room devicename>\(lastSelectedRoom.deviceName)")
        
        self.bthw.initDevice(lastSelectedRoom.deviceName)
         self.refreshWithDevice()
      
        
    }
    
    
    func refreshWithDevice() {
        
        print("Checking for connection11 ")
        if( bthw.isConnected()) {
            bthw.verifyPwd()
            let numDevices = self.bthw.getNumberOfDevices()
            print("NUmber of devices.....\(numDevices)")
            var statuses:[UInt8] = self.bthw.getAllStatus()
             self.bthw.closeDevice()
            if (statuses.count == 0) {
                print("No status")
                return;
            }
            for i in 3 ..< statuses.count {
                let deviceid = i-3;
                let status = statuses[i];
                print("Status......\(deviceid)....\(status)")
            }
            
        } else {
            print("Not Connected ")
        }
    }
    func constructRoomList() {
        rooms = dbOperation.getRoomList()
        for i in 0 ..< rooms.count {
            roomNames.append([rooms[i].roomName,""])
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
            for i in 0 ..< interfacesArray.count  {
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
        } else if( sender.tag >= 201 && sender.tag <= 202) {
            performSegueWithIdentifier("ConfigSegueId", sender: sender)
        }else if( sender.tag == 203 ) {
            performSegueWithIdentifier("GroupSegueId", sender: sender)
        }else if( sender.tag == 204 ) {
            performSegueWithIdentifier("ShedulerSegueId", sender: sender)
        } else if( sender.tag >= 101 && sender.tag <= 104) {
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
   
    override func menuItemClicked(sourceMenu:UIView, rowIndex: Int) {
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
            let lastroom = rooms[rowIndex];
            dbOperation.setLastSelectedRoom(lastroom)
            populateRoomPanel(lastroom)
        }
        print("ss434ssss")
        
        
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject!) {
        print("Segue called from  view controller \(segue.identifier)  \(sender.tag)")
        if (segue.identifier == "HtmlSegueId") {
            let svc = segue.destinationViewController as! HtmlViewController;
            var title = "Help";
            var file = "help"
            if( sender.tag == 2) {
                file = "about"
                title = "About"
            }
            svc.setHtmlFile(title, htmlfile: file)
        } else if ( segue.identifier == "ConfigSegueId"){
            let svc = segue.destinationViewController as! ConfigViewController;
            svc.setRoomDeviceName(lightsComp, name: selectedRoomDeviceName, tag: sender.tag)
            if( sender.tag == 201) {
                let selindex = lightsComp.getSelectedIndex();
                let deviceDAO = dbOperation.getLights(selectedRoomDeviceName)[selindex];
                svc.updateDAO(deviceDAO, indexAt: selindex);
            } else if( sender.tag == 202) {
                let selindex = devicesComp.getSelectedIndex();
                let deviceDAO = dbOperation.getDevices(selectedRoomDeviceName)[selindex];
                svc.updateDAO(deviceDAO, indexAt: selindex);
            }
        } else if ( segue.identifier == "GroupSegueId"){
            let svc = segue.destinationViewController as! GroupViewController;
            svc.setRoomDeviceName(selectedRoomDeviceName)
            if( sender.tag == 203) {
                let selindex = groupsComp.getSelectedIndex();
                let groupDAO = dbOperation.getGroups(selectedRoomDeviceName)[selindex];
                svc.updateDAO(groupDAO, indexAt: selindex);
            }
        } else if ( segue.identifier == "ShedulerSegueId"){
            let svc = segue.destinationViewController as! SchedulerViewController;
            svc.setRoomDeviceName(selectedRoomDeviceName)
            if( sender.tag == 204) {
                let selindex = schedulersComp.getSelectedIndex();
                let schedulerDAO = dbOperation.getSchedulers(selectedRoomDeviceName)[selindex];
                svc.updateDAO(schedulerDAO);
            }
        }
    }

    func backgroundThread(delay: Double = 0.0, background: (() -> Void)? = nil, completion: (() -> Void)? = nil) {
        dispatch_async(dispatch_get_global_queue(Int(QOS_CLASS_BACKGROUND.rawValue), 0)) {
            if(background != nil){ background!(); }
            
            let popTime = dispatch_time(DISPATCH_TIME_NOW, Int64(delay * Double(NSEC_PER_SEC)))
            dispatch_after(popTime, dispatch_get_main_queue()) {
                if(completion != nil){ completion!(); }
            }
        }
    }
}

