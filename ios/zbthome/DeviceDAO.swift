//
//  RoomDAO.swift
//  zbthome
//
//  Created by Mackbook on 04/08/16.
//  Copyright © 2016 Mackbook. All rights reserved.
//

import UIKit

class DeviceDAO: NSObject, NSCoding {

    var deviceName:String = ""
    var deviceId:Int = -1
    var isdimmable:Bool = true
    var deviceType:String = ""
    var statusValue:Int = 0;
    
    init(deviceName:String, deviceId:Int, isdimmable:Bool, deviceType:String) {
        self.deviceName = deviceName
        self.deviceId = deviceId
        self.isdimmable = isdimmable
        self.deviceType = deviceType
        super.init()
    }
    
    required internal init(coder decoder: NSCoder) {
        deviceName = decoder.decodeObjectForKey("deviceName") as! String
        deviceId = decoder.decodeObjectForKey("deviceId") as! Int
        isdimmable = decoder.decodeObjectForKey("isdimmable") as! Bool
        deviceType = decoder.decodeObjectForKey("deviceType") as! String
        statusValue = decoder.decodeObjectForKey("statusValue") as! Int
    }
    
    internal func encodeWithCoder(coder: NSCoder) {
        coder.encodeObject(deviceName, forKey: "deviceName")
        coder.encodeObject(deviceId, forKey: "deviceId")
        coder.encodeObject(isdimmable, forKey: "isdimmable")
        coder.encodeObject(deviceType, forKey: "deviceType")
        coder.encodeObject(statusValue, forKey: "statusValue")
    }
}
