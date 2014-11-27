function sample_json_templates(){
	json={
		'dirs':
		[
			{
				'path':'Demos/ListTokenDemo',
				'ls':
				[
					{'name':'fileA.css'},
					{'name':'fileB.phtml','tokens':
						[
							'filename:l:path/to/file:.',
							'var:u:some var',
							'list:some list'
						]
					},
					{'name':'fileC.php'},
					{'name':'fileD.js','tokens':
						[
							'filename:l:path/to/file:.',
							'var:u:some var',
							'list:some list'
						]
					},
					{'name':'fileE.html'},
					{'name':'fileF.txt'}
				],
				'includes':
				[
					'test/*/allFiles',
					'[css]/stuff*yep/sub.css',
					'[css]/includethis.*'
				],
				'hidden':
				[
					'_file1.css','_file2.phtml','_file3.php'
				]
			},
			{'path':'Demos/TestInclude','ls':
				[
					{'name':'fileA.css'},
					{'name':'fileB.phtml'}
				],
				'includes':
				[
					'test/*/allFiles'
				],
				'hidden':
				[
					'_file1.css'
				]
			},
			{'path':'Demos/TestVarOptions','ls':
				[
					{'name':'fileA.css'},
					{'name':'fileF.txt'}
				]
			},
			{'path':'Magento/MageStarterKit','ls':
				[
					{'name':'fileA.css'},
					{'name':'fileB.phtml'},
					{'name':'fileC.php','tokens':
						[
							'filename:l:path/to/file:.',
							'var:u:some var',
							'list:some list'
						]
					}
				]
			}
		],
		'hidden':
		[
			'Demos/_HiddenFolder',
			'Demos/_AnotherTemplate'
		]
	};
	return json;
}