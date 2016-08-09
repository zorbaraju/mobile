//
//  RoomDAO.swift
//  zbthome
//
//  Created by Mackbook on 04/08/16.
//  Copyright © 2016 Mackbook. All rights reserved.
//

import UIKit

class GroupDAO: NSObject, NSCoding {

    var name:String = ""
    
    init(name:String, deviceId:Int) {
        self.name = name
        super.init()
    }
    
    required internal init(coder decoder: NSCoder) {
        name = decoder.decodeObjectForKey("name") as! String
    }
    
    internal func encodeWithCoder(coder: NSCoder) {
        coder.encodeObject(name, forKey: "name")
    }
}
