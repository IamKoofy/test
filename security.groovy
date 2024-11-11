import json
from ansible.module_utils.basic import AnsibleModule

def main():
    module = AnsibleModule(argument_spec={})
    # Access the ansible_env variable
    ansible_env = module.params.get('ansible_env', {})
    
    # Print ansible_env as a JSON string for better readability
    print(json.dumps(ansible_env, indent=4))

if __name__ == '__main__':
    main()
