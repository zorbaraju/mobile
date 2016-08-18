//
//  RoomDAO.swift
//  zbthome
//
//  Created by Mackbook on 04/08/16.
//  Copyright © 2016 Mackbook. All rights reserved.
//

import UIKit

class RoomDAO: NSObject, NSCoding {

    var deviceName:String = ""
    var roomName:String = ""
    var numDevices:Int = 10;
    
    init(deviceName:String, roomName:String, numDevices:Int) {
        self.deviceName = deviceName
        self.roomName = roomName
        self.numDevices = numDevices
        super.init()
    }
    
    required public init(coder decoder: NSCoder) {
        deviceName = decoder.decodeObjectForKey("deviceName") as! String
        roomName = decoder.decodeObjectForKey("roomName") as! String
        if( decoder.decodeObjectForKey("numDevices") != nil) {
            numDevices = decoder.decodeObjectForKey("numDevices") as! Int
        }
    }
    
    public func encodeWithCoder(coder: NSCoder) {
        coder.encodeObject(deviceName, forKey: "deviceName")
        coder.encodeObject(roomName, forKey: "roomName")
        coder.encodeObject(numDevices, forKey: "numDevices")
    }
}
