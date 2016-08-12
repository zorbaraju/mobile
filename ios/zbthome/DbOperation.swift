//
//  DbOperation.swift
//  zbthome
//
//  Created by Mackbook on 06/08/16.
//  Copyright © 2016 Mackbook. All rights reserved.
//
import UIKit

class DBOperation {
    
    let roomsKey = "rooms"
    let lastSelectedRoomKey = "lastselectedroom"
    var phoneMemory:NSUserDefaults!;
    init() {
        phoneMemory = NSUserDefaults.standardUserDefaults()
    }
    
   
    func setLastSelectedRoom(lastSelectedRoom:String) {
        phoneMemory.setObject(lastSelectedRoom, forKey: lastSelectedRoomKey)
    }
    
    func isRoomExist(deviceName:String)-> Bool {
         var rooms = getRoomList();
        for var i = 0; i < rooms.count; i += 1 {
            let room = rooms[i];
            if( room.deviceName == deviceName) {
                return true;
            }
        }
        return false;
    }
    
    func getLastSelectedRoom()->String {
        var lastselectedroom:String = "No Rooms";
        if( phoneMemory.stringForKey("lastselectedroom") != nil) {
            lastselectedroom = phoneMemory.stringForKey(lastSelectedRoomKey)!;
        }
        print("Last selected room is \(lastselectedroom)")
        return lastselectedroom
    }
    
    func getRoomList()->[RoomDAO] {
        var rooms:[RoomDAO] = [RoomDAO]()
        let temprooms = phoneMemory.objectForKey(roomsKey)
        if let unarchivedObject = temprooms as? NSData {
            rooms = (NSKeyedUnarchiver.unarchiveObjectWithData(unarchivedObject) as? [RoomDAO]!)!
        }
        return rooms
    }
    
    func addRoom(room:RoomDAO) {
        var rooms = getRoomList();
        rooms.append(room)
        let archivedObject = NSKeyedArchiver.archivedDataWithRootObject(rooms as NSArray)
        phoneMemory.setObject(archivedObject, forKey: roomsKey)
    }
    
    func removeRoom(roomIndex:Int) {
        var rooms = getRoomList();
        rooms.removeAtIndex(roomIndex)
        let archivedObject = NSKeyedArchiver.archivedDataWithRootObject(rooms as NSArray)
        phoneMemory.setObject(archivedObject, forKey: roomsKey)
    }
    
    func getLights(roomDeviceName:String)->[DeviceDAO] {
        var lights:[DeviceDAO] = [DeviceDAO]()
        print("kkkkkkkkkk"+"\(roomDeviceName)lights")
        let temp = phoneMemory.objectForKey("\(roomDeviceName)lights")
        if let unarchivedObject = temp as? NSData {
            lights = (NSKeyedUnarchiver.unarchiveObjectWithData(unarchivedObject) as? [DeviceDAO]!)!
        }
        return lights
    }
    func addLight(roomDeviceName:String, light:DeviceDAO) {
         print("kkkkkkkdssfsfdsfskkk"+"\(roomDeviceName)lights")
        var lights = getLights(roomDeviceName);
        lights.append(light)
        let archivedObject = NSKeyedArchiver.archivedDataWithRootObject(lights as NSArray)
        phoneMemory.setObject(archivedObject, forKey: "\(roomDeviceName)lights")
    }
    
    func removeLight(roomDeviceName:String, indexAt:Int) {
        var lights = getLights(roomDeviceName);
        print("size of listhfff..before..\(lights.count)")
        lights.removeAtIndex(indexAt)
        print("size of listhfff....\(lights.count)")
        let archivedObject = NSKeyedArchiver.archivedDataWithRootObject(lights as NSArray)
        phoneMemory.setObject(archivedObject, forKey: "\(roomDeviceName)lights")
    }
    
    func getDevices(roomDeviceName:String)->[DeviceDAO] {
        var lights:[DeviceDAO] = [DeviceDAO]()
        print("kkkkkkkkkk"+"\(roomDeviceName)devices")
        let temp = phoneMemory.objectForKey("\(roomDeviceName)devices")
        if let unarchivedObject = temp as? NSData {
            lights = (NSKeyedUnarchiver.unarchiveObjectWithData(unarchivedObject) as? [DeviceDAO]!)!
        }
        return lights
    }
    func addDevice(roomDeviceName:String, light:DeviceDAO) {
        print("kkkkkkkdssfsfdsfskkk"+"\(roomDeviceName)devices")
        var lights = getDevices(roomDeviceName);
        lights.append(light)
        let archivedObject = NSKeyedArchiver.archivedDataWithRootObject(lights as NSArray)
        phoneMemory.setObject(archivedObject, forKey: "\(roomDeviceName)devices")
    }
    
    func removeDevice(roomDeviceName:String, indexAt:Int) {
        var lights = getDevices(roomDeviceName);
        print("size of listhfff..before..\(lights.count)")
        lights.removeAtIndex(indexAt)
        print("size of listhfff....\(lights.count)")
        let archivedObject = NSKeyedArchiver.archivedDataWithRootObject(lights as NSArray)
        phoneMemory.setObject(archivedObject, forKey: "\(roomDeviceName)devices")
    }
    
    func getGroups(roomDeviceName:String)->[GroupDAO] {
        var groups:[GroupDAO] = [GroupDAO]()
        let temp = phoneMemory.objectForKey(roomDeviceName+"groups")
        if let unarchivedObject = temp as? NSData {
            groups = (NSKeyedUnarchiver.unarchiveObjectWithData(unarchivedObject) as? [GroupDAO]!)!
        }
        return groups
    }
    
    func getSchedulers(roomDeviceName:String)->[SchedulerDAO] {
        var schedulars:[SchedulerDAO] = [SchedulerDAO]()
        let temp = phoneMemory.objectForKey(roomDeviceName+"schedulers")
        if let unarchivedObject = temp as? NSData {
            schedulars = (NSKeyedUnarchiver.unarchiveObjectWithData(unarchivedObject) as? [SchedulerDAO]!)!
        }
        return schedulars
    }
}