<<filename:c:app/code/[codepool]/[Prefix]/[Module]/Block:"SampleBlock">>
<<var:n:module prefix => [Prefix]>>
<<var:c:module name => [Module]>>
<<var:l:module prefix => [prefix]>>
<<var:l:module name => [module]>>
<<var:l:code pool => [codepool]>>

<?php
//blocks will extend Mage_Core_Block_Template
class [Prefix]_[Module]_Block_SampleBlock extends Mage_Core_Block_Template{
	function getSomething(){
		//example: calling a method from SampleModel.php
		return Mage::getModel('[module]/samplemodel')->getName();
	}
    
    function getConfig() {
        return Mage::getModel('[module]/samplemodel')->getConfigSettings();
    }   
}
?>