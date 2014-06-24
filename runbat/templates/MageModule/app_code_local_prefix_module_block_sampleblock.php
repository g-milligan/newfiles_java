<<filename:c:app/code/local/[Prefix]/[Module]/Block:"SampleBlock">>
<<var:n:module prefix => [Prefix]>>
<<var:c:module name => [Module]>>
<<var:l:module prefix => [prefix]>>
<<var:l:module name => [module]>>
<?php
//blocks will extend Mage_Core_Block_Template
class [Prefix]_[Module]_Block_SampleBlock extends Mage_Core_Block_Template{
	function getSomething(){
		//example: calling a method from SampleModel.php
		return Mage::getModel('[prefix][module]/samplemodel')->getName();
	}
}
?>