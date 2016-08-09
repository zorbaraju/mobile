//
//  ViewController.swift
//  second
//
//  Created by Mackbook on 25/07/16.
//  Copyright © 2016 Mackbook. All rights reserved.
//

import UIKit
import SystemConfiguration
import SystemConfiguration.CaptiveNetwork

class ViewController: UIViewController {
    
    var selectedRoomDeviceName:String!;
    var lightsComp:FV!;
    var devicesComp:FV!;
    var groupsComp:FV!;
    var schedulersComp:FV!;

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
        lightsComp = FV(frame: verticalview.bounds, title:"Lights",tag:1);
        lightsComp.registerListeners(self, methodName: "buttonClicked:");
        verticalview.addArrangedSubview(lightsComp)
        devicesComp = FV(frame: verticalview.bounds, title:"Devices",tag:2);
        devicesComp.registerListeners(self, methodName: "buttonClicked:");
        verticalview.addArrangedSubview(devicesComp)
        groupsComp = FV(frame: verticalview.bounds, title:"Groups",tag:3);
        groupsComp.registerListeners(self, methodName: "buttonClicked:");
        verticalview.addArrangedSubview(groupsComp)
        schedulersComp = FV(frame: verticalview.bounds, title:"Schedulers",tag:4);
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
                let compData:CollapseCompData = CollapseCompData(name: lights[i].deviceName);
                devicesComp.addComp(compData)
            }
            let groups:[GroupDAO] = dbOperation.getGroups(lastSelectedRoom);
            for var i = 0; i < groups.count; i += 1 {
                let compData:CollapseCompData = CollapseCompData(name: lights[i].deviceName);
                groupsComp.addComp(compData)
            }
            let schedulers:[SchedulerDAO] = dbOperation.getSchedulers(lastSelectedRoom);
            for var i = 0; i < schedulers.count; i += 1 {
                let compData:CollapseCompData = CollapseCompData(name: lights[i].deviceName);
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
        if( sender.tag >= 1 && sender.tag <= 4) {
            performSegueWithIdentifier("ConfigSegueId", sender: sender)
        } else if( sender.tag >= 101 && sender.tag <= 104) {
            print("remove operation of collapse panel \(sender.tag)")
            if( sender.tag == 101) {
                let selindex = lightsComp.getSelectedIndex();
                print("selectindex....\(selindex)   "+selectedRoomDeviceName)
                dbOperation.removeLight(selectedRoomDeviceName, indexAt: selindex);
                lightsComp.removeComp()
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
        print("Segue called from  view controller \(segue.identifier)")
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
            svc.setRoomDeviceName(lightsComp, name: selectedRoomDeviceName)
        }
    }
    
}

