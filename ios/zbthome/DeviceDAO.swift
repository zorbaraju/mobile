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
    
    init(deviceName:String, deviceId:Int) {
        self.deviceName = deviceName
        self.deviceId = deviceId
        super.init()
    }
    
    required internal init(coder decoder: NSCoder) {
        deviceName = decoder.decodeObjectForKey("deviceName") as! String
        deviceId = decoder.decodeObjectForKey("deviceId") as! Int
    }
    
    internal func encodeWithCoder(coder: NSCoder) {
        coder.encodeObject(deviceName, forKey: "deviceName")
        coder.encodeObject(deviceId, forKey: "deviceId")
       
    }
}
