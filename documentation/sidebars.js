/** @type {import('@docusaurus/plugin-content-docs').SidebarsConfig} */
const sidebars = {
  docsSidebar: [
    {
      type: 'doc',
      id: 'index',
      label: '🏠 Home',
    },
    {
      type: 'category',
      label: 'Getting Started',
      collapsed: false,
      items: [
        'overview',
        'tech-stack',
        'configuration',
      ],
    },
    {
      type: 'category',
      label: 'Architecture & Design',
      collapsed: false,
      items: [
        'architecture',
        'features',
      ],
    },
    {
      type: 'category',
      label: 'Developer Guide',
      collapsed: false,
      items: [
        'development',
        'examples',
        'allure',
      ],
    },
    {
      type: 'category',
      label: 'Operations',
      collapsed: false,
      items: [
        'cicd',
        'contributing',
      ],
    },
  ],
};

export default sidebars;
