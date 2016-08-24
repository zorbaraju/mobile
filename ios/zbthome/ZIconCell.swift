//
//  ZIconCell.swift
//  second
//
//  Created by Mackbook on 31/07/16.
//  Copyright © 2016 Mackbook. All rights reserved.
//

import UIKit

class ZIconCell: UICollectionViewCell {
    var labelName:String!
    var imageName:String!
    
    @IBOutlet var imageView: UIImageView!
    var view: UIView!
    
    @IBOutlet var titleLabel: UILabel!
    override init(frame: CGRect) {
        // 1. setup any properties here
        
        // 2. call super.init(frame:)
        super.init(frame: frame)
        
        // 3. Setup view from .xib file
        xibSetup()
    }
    
    required init?(coder aDecoder: NSCoder) {
        // 1. setup any properties here
        
        // 2. call super.init(coder:)
        super.init(coder: aDecoder)
        
        // 3. Setup view from .xib file
        xibSetup()
    }
    
    func setOn(on:Bool) {
        if( on ) {
            imageView.backgroundColor = UIColor.orangeColor();
        } else {
            imageView.backgroundColor = UIColor.blackColor();
        }
    }
    
    func setOffline() {
        imageView.backgroundColor = UIColor.grayColor();
    }
    func setLabelAndImage(title: String, imageName:String) {
        self.labelName = title
        self.imageName = imageName
        imageView.image = UIImage(named: imageName)
        titleLabel.text = labelName
    }
    
    func xibSetup() {
        view = loadViewFromNib()
        
        // use bounds not frame or it'll be offset
        view.frame = bounds
        
        // Make the view stretch with containing view
        view.autoresizingMask = [UIViewAutoresizing.FlexibleWidth, UIViewAutoresizing.FlexibleHeight]
        // Adding custom subview on top of our view (over any custom drawing > see note below)
        addSubview(view)
        setOffline();
        makeCircleImage(imageView)
    }
    
    func loadViewFromNib() -> UIView {
        
        let bundle = NSBundle(forClass: self.dynamicType)
        let nib = UINib(nibName: "ZIConCell", bundle: bundle)
        let view = nib.instantiateWithOwner(self, options: nil)[0] as! UIView
        return view
    }
    
    func setIConSelect(select:Bool) {
        if( select) {
            imageView.layer.borderWidth = 0.15
            imageView.layer.borderColor = UIColor.blackColor().CGColor
        } else {
            imageView.layer.borderWidth = 0
            imageView.layer.borderColor = UIColor.blackColor().CGColor
        }
    }
    func makeCircleImage(image:UIImageView) {
        image.frame.size.width = image.frame.size.height
        image.layer.cornerRadius=image.frame.height/2;
    }
}
