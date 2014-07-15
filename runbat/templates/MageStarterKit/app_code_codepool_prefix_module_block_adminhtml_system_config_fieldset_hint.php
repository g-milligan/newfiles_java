<<filename:l:app/code/[codepool]/[Prefix]/[Module]/Block/Adminhtml/System/Config/Fieldset:"Hint">>
<<var:n:module prefix => [Prefix]>>
<<var:l:module prefix => [prefix]>>
<<var:c:module name => [Module]>>
<<var:l:module name => [module]>>
<<var:l:code pool => [codepool]>>

<?php
/**
 * Renderer for Hint Banner in System Configuration
 */
class [Prefix]_[Module]_Block_Adminhtml_System_Config_Fieldset_Hint
    extends Mage_Adminhtml_Block_Abstract
    implements Varien_Data_Form_Element_Renderer_Interface
{
    protected $_template = '[module]/system/config/fieldset/hint.phtml';

    /**
     * Render fieldset html
     *
     * @param Varien_Data_Form_Element_Abstract $element
     * @return string
     */
    public function render(Varien_Data_Form_Element_Abstract $element)
    {
        return $this->toHtml();
    }
    public function get[Module]Version()
    {
        return (string) Mage::getConfig()->getNode('modules/[Prefix]_[Module]/version');
    }

}