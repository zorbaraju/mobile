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
    let lastvisitedroomKey = "lastvisitedroom"
    var phoneMemory:NSUserDefaults!;
    
    class func getInstance()->DBOperation {
        struct Static {
            static let instance = DBOperation()
        }
        return Static.instance
    }
    
    private init() {
        phoneMemory = NSUserDefaults.standardUserDefaults()
    }
    
   
    
    func isRoomExist(deviceName:String)-> Bool {
         var rooms = getRoomList();
        for i in 0 ..< rooms.count {
            let room = rooms[i];
            if( room.deviceName == deviceName) {
                return true;
            }
        }
        return false;
    }

    func setLastSelectedRoom(lastSelectedRoom:RoomDAO) {
        let archivedObject = NSKeyedArchiver.archivedDataWithRootObject(lastSelectedRoom as NSObject)
        phoneMemory.setObject(archivedObject, forKey: lastvisitedroomKey)
    }

    func getLastSelectedRoom()->RoomDAO {
        var room:RoomDAO = RoomDAO(deviceName: "", roomName: "No Room", numDevices: 10);
        let obj = phoneMemory.objectForKey(lastvisitedroomKey);
        if let unarchivedObject = obj as? NSData {
            room = (NSKeyedUnarchiver.unarchiveObjectWithData(unarchivedObject) as? RoomDAO!)!
        }
        return room
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
    
    func updateLight(roomDeviceName:String, light:DeviceDAO, indexAt:Int) {
        print("kkkkkkkdssfsfdsfskkk"+"\(roomDeviceName)lights...indexAt \(indexAt)")
        var lights = getLights(roomDeviceName);
        lights[indexAt] = light
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
    
    func updateDevice(roomDeviceName:String, light:DeviceDAO, indexAt:Int) {
        print("kkkkkkkdssfsfdsfskkk"+"\(roomDeviceName)devices")
        var lights = getDevices(roomDeviceName);
        lights[indexAt] = light
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
        var lights:[GroupDAO] = [GroupDAO]()
        print("kkkkkkkkkk"+"\(roomDeviceName)groups")
        let temp = phoneMemory.objectForKey("\(roomDeviceName)groups")
        if let unarchivedObject = temp as? NSData {
            lights = (NSKeyedUnarchiver.unarchiveObjectWithData(unarchivedObject) as? [GroupDAO]!)!
        }
        return lights
    }
    func addGroup(roomDeviceName:String, light:GroupDAO) {
        print("kkkkkkkdssfsfdsfskkk"+"\(roomDeviceName)groups")
        var lights = getGroups(roomDeviceName);
        lights.append(light)
        let archivedObject = NSKeyedArchiver.archivedDataWithRootObject(lights as NSArray)
        phoneMemory.setObject(archivedObject, forKey: "\(roomDeviceName)groups")
    }
    
    func updateGroup(roomDeviceName:String, group:GroupDAO, indexAt:Int) {
        print("kkkkkkkdssfsfdsfskkk"+"\(roomDeviceName)group...indexAt \(indexAt)")
        var groups = getGroups(roomDeviceName);
        groups[indexAt] = group
        let archivedObject = NSKeyedArchiver.archivedDataWithRootObject(groups as NSArray)
        phoneMemory.setObject(archivedObject, forKey: "\(roomDeviceName)groups")
    }
    
    func removeGroup(roomDeviceName:String, indexAt:Int) {
        var lights = getGroups(roomDeviceName);
        print("size of listhfff..before..\(lights.count)")
        lights.removeAtIndex(indexAt)
        print("size of listhfff....\(lights.count)")
        let archivedObject = NSKeyedArchiver.archivedDataWithRootObject(lights as NSArray)
        phoneMemory.setObject(archivedObject, forKey: "\(roomDeviceName)groups")
    }
    
    func getSchedulers(roomDeviceName:String)->[SchedulerDAO] {
        var lights:[SchedulerDAO] = [SchedulerDAO]()
        print("kkkkkkkkkk"+"\(roomDeviceName)schedulers")
        let temp = phoneMemory.objectForKey("\(roomDeviceName)schedulers")
        if let unarchivedObject = temp as? NSData {
            lights = (NSKeyedUnarchiver.unarchiveObjectWithData(unarchivedObject) as? [SchedulerDAO]!)!
        }
        return lights
    }
    func addScheduler(roomDeviceName:String, light:SchedulerDAO) {
        print("kkkkkkkdssfsfdsfskkk"+"\(roomDeviceName)schedulers")
        var lights = getSchedulers(roomDeviceName);
        lights.append(light)
        let archivedObject = NSKeyedArchiver.archivedDataWithRootObject(lights as NSArray)
        phoneMemory.setObject(archivedObject, forKey: "\(roomDeviceName)schedulers")
    }
    
    func removeScheduler(roomDeviceName:String, indexAt:Int) {
        var lights = getSchedulers(roomDeviceName);
        print("size of listhfff..before..\(lights.count)")
        lights.removeAtIndex(indexAt)
        print("size of listhfff....\(lights.count)")
        let archivedObject = NSKeyedArchiver.archivedDataWithRootObject(lights as NSArray)
        phoneMemory.setObject(archivedObject, forKey: "\(roomDeviceName)schedulers")
    }

}